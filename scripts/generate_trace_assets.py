#!/usr/bin/env python3
"""Generate the specification's 84 tracing assets; no Android runtime dependencies.

Run with Python 3.10+ after installing requirements-imagegen.txt. The default run
processes the entire library. --prepare and --validate-only make no API calls.
Credentials are read only by the SDK, never serialized or included in diagnostics.
"""
from __future__ import annotations

import argparse
import base64
from collections import Counter
from concurrent.futures import ThreadPoolExecutor, as_completed
from datetime import datetime, timezone
import io
import json
import logging
import os
from pathlib import Path
import random
import re
import sys
import tempfile
import threading
import time

ROOT = Path(__file__).resolve().parents[1]
SPEC = ROOT / "docs/cute_animals_trace_draw_work_mode_prompt.md"
MANIFEST = ROOT / "scripts/trace_assets_manifest.json"
REPORT = ROOT / "scripts/generation_report.json"
ANDROID_MANIFEST = ROOT / "app/src/main/assets/templates_manifest.json"
ASSETS = ROOT / "app/src/main/assets"
MODEL = "gpt-image-2.5-flare"
SIZE = "1024x1024"
CATEGORIES = {
    "Cats": ("cats", "cat", 15), "Dogs": ("dogs", "dog", 15),
    "Bunnies": ("bunnies", "bunny", 10), "Pandas": ("pandas", "panda", 8),
    "Foxes": ("foxes", "fox", 8), "Bears": ("bears", "bear", 8),
    "Kawaii Animals": ("kawaii", "kawaii", 10),
    "Baby Animals": ("baby_animals", "baby", 10),
}
ANDROID_CATEGORIES = dict(zip(
    (v[0] for v in CATEGORIES.values()),
    ("Cats", "Dogs", "Bunnies", "Pandas", "Foxes", "Bears", "Kawaii", "BabyAnimals"),
))
# Verbatim global instructions from the authoritative specification. This value
# is assembled once, identical across every request; individual descriptions are
# kept separately and never paraphrased. The user's explicit transparency rule
# tightens the specification's optional white-background fallback.
GLOBAL_STYLE_PROMPT = ""
DIFFICULTY_REQUIREMENTS = {}


class ValidationError(Exception):
    """Only locally authored, credential-free messages belong in this exception."""


def parse_spec(text):
    global GLOBAL_STYLE_PROMPT, DIFFICULTY_REQUIREMENTS
    sections = {}
    heading = None
    for line in text.splitlines():
        if line.startswith("## "):
            heading = line[3:].strip()
            if heading in sections:
                raise ValidationError("Duplicate specification heading")
            sections[heading] = []
        elif heading:
            sections[heading].append(line)
    try:
        style = "\n".join(sections["Global rules for EVERY image"]).strip()
        difficulty = "\n".join(sections["Difficulty"])
        featured = re.findall(r"^([a-z]+_\d{3})\.png$",
                              "\n".join(sections["Featured images"]), re.M)
    except KeyError:
        raise ValidationError("Required specification section missing") from None
    DIFFICULTY_REQUIREMENTS = dict(re.findall(
        r"^(EASY|MEDIUM|DETAILED): (.+)$", difficulty, re.M))
    if len(DIFFICULTY_REQUIREMENTS) != 3 or not style or len(featured) != 12:
        raise ValidationError("Incomplete style, difficulty, or featured specification")
    GLOBAL_STYLE_PROMPT = (
        "Production tracing template for a phone camera overlay.\n" + style +
        "\nUse a genuinely transparent background. No white canvas or checkerboard."
        " No color or gray shading. Easy physical tracing on paper."
        " Generate one image, never a collage or contact sheet."
    )
    records = []
    asset_pattern = r"([a-z]+_\d{3})\.png — (.+?) — (Easy|Medium|Detailed) — (.+)"
    for title, (category, prefix, count) in CATEGORIES.items():
        lines = sections.get(f"{title} — {count}", [])
        category_records = []
        for line in lines:
            if not line.strip():
                continue
            match = re.fullmatch(asset_pattern, line)
            if not match:
                raise ValidationError(f"Unparseable asset line in {category}")
            asset_id, name, level, description = match.groups()
            output = f"{asset_id}.webp"
            category_records.append({
                "id": asset_id, "originalFilename": f"{asset_id}.png",
                "outputFilename": output, "displayName": name,
                "category": category, "difficulty": level,
                "featured": asset_id in featured,
                "generationDescription": description,
                "shortDescription": description,
                "androidAssetPath": f"templates/{category}/{output}",
            })
        expected_ids = {f"{prefix}_{i:03}" for i in range(1, count + 1)}
        if len(category_records) != count or {r["id"] for r in category_records} != expected_ids:
            raise ValidationError(f"Specification count/ID mismatch in {category}")
        records.extend(category_records)
    # Catch undeclared records outside the eight recognized category sections.
    all_asset_lines = re.findall(r"^.+\.png — .+$", text, re.M)
    if len(records) != 84 or len(all_asset_lines) != 84:
        raise ValidationError("Specification does not resolve to exactly 84 assets")
    if len({r["id"] for r in records}) != 84 or not set(featured) <= {r["id"] for r in records}:
        raise ValidationError("Duplicate or unknown asset ID")
    return records


def atomic_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    fd, name = tempfile.mkstemp(prefix=".trace-", suffix=".tmp", dir=path.parent)
    try:
        with os.fdopen(fd, "w", encoding="utf-8") as stream:
            json.dump(value, stream, indent=2, ensure_ascii=False)
            stream.write("\n")
            stream.flush()
            os.fsync(stream.fileno())
        os.replace(name, path)
    finally:
        Path(name).unlink(missing_ok=True)


def destination(asset):
    expected = f"templates/{asset['category']}/{asset['id']}.webp"
    if asset["androidAssetPath"] != expected or asset["outputFilename"] != f"{asset['id']}.webp":
        raise ValidationError("Manifest filename/category/path mismatch")
    path = ASSETS / expected
    if not path.resolve().is_relative_to(ASSETS.resolve()):
        raise ValidationError("Asset path escapes Android assets directory")
    return path


def check_pixels(image, production=False):
    from PIL import Image, ImageChops
    if image.size != (1024, 1024):
        raise ValidationError("Image dimensions are not 1024x1024")
    rgba = image.convert("RGBA")
    alpha = rgba.getchannel("A")
    if alpha.getextrema()[0] == 255:
        raise ValidationError("Transparent background missing")
    composite = Image.new("RGBA", rgba.size, "white")
    composite.alpha_composite(rgba)
    rgb = composite.convert("RGB")
    gray = rgb.convert("L")
    ink = gray.point(lambda p: 255 if p < 160 else 0)
    bbox = ink.getbbox()
    ink_pixels = ink.histogram()[255]
    if bbox is None or ink_pixels < 1500 or ink_pixels > 400000:
        raise ValidationError("Image is empty or contains excessive solid fill")
    x0, y0, x1, y1 = bbox
    if min(x0, y0, 1024 - x1, 1024 - y1) < (30 if production else 2):
        raise ValidationError("Subject is cropped or lacks empty margins")
    if max(x1 - x0, y1 - y0) < 500:
        raise ValidationError("Subject is too small for readable tracing")
    occupancy = max(x1 - x0, y1 - y0) / 1024
    if production and not 0.65 <= occupancy <= 0.75:
        raise ValidationError("Subject does not occupy 65–75% of the canvas")
    if production and (abs((x0+x1)/2-512) > 32 or abs((y0+y1)/2-512) > 32):
        raise ValidationError("Subject is not centered")
    r, g, b = rgb.split()
    chroma = ImageChops.lighter(ImageChops.difference(r, g), ImageChops.difference(g, b))
    if sum(chroma.histogram()[25:]) > 100:
        raise ValidationError("Color detected in monochrome line art")
    # Gray antialiasing along strokes is valid; broad gray fills are not.
    histogram = gray.histogram()
    if sum(histogram[40:215]) > 0.35 * ink_pixels:
        raise ValidationError("Excessive gray shading or soft strokes")
    return {"dimensions": [1024, 1024], "transparencyDetected": True,
            "inkPixels": ink_pixels, "subjectBounds": list(bbox),
            "canvasOccupancy": round(max(x1-x0, y1-y0) / 1024, 4)}


def normalize_canvas(rgba):
    """Fit existing line art on a transparent 1024-square canvas, without cropping.

    The API sometimes ignores margin instructions. This layout-only conversion
    keeps the alpha channel, uniformly scales the subject, and centers it. It
    does not redraw anatomy, change colors, or synthesize any image content.
    WebP encoding is lossless relative to this normalized RGBA image.
    """
    from PIL import Image
    stats = check_pixels(rgba)
    x0, y0, x1, y1 = stats["subjectBounds"]
    if (0.65 <= stats["canvasOccupancy"] <= 0.75 and
            abs((x0+x1)/2-512) <= 32 and abs((y0+y1)/2-512) <= 32):
        return rgba
    # Some API PNGs contain opaque pure-white pixels in otherwise transparent
    # margins. Center visible ink rather than invisible white padding. Include
    # a small guard around the ink for antialiased edge pixels.
    bbox = (max(0, x0-3), max(0, y0-3), min(1024, x1+3), min(1024, y1+3))
    subject = rgba.crop(bbox)
    scale = 738 / max(subject.size)
    subject = subject.resize(tuple(max(1, round(v*scale)) for v in subject.size), Image.Resampling.LANCZOS)
    canvas = Image.new("RGBA", (1024, 1024), (0, 0, 0, 0))
    canvas.alpha_composite(subject, ((1024-subject.width)//2, (1024-subject.height)//2))
    check_pixels(canvas, production=True)
    return canvas


def is_lossless_webp(path):
    raw = path.read_bytes()
    if raw[:4] != b"RIFF" or raw[8:12] != b"WEBP":
        return False
    position = 12
    found = False
    while position + 8 <= len(raw):
        tag = raw[position:position+4]
        length = int.from_bytes(raw[position+4:position+8], "little")
        if position + 8 + length > len(raw):
            return False
        if tag == b"VP8 ":
            return False
        if tag == b"VP8L":
            found = True
        position += 8 + length + (length % 2)
    return found


def validate_webp(path, asset=None):
    from PIL import Image
    if asset is not None and path != destination(asset):
        raise ValidationError("Wrong filename or destination category")
    if not path.is_file() or path.stat().st_size == 0:
        raise ValidationError("Missing or zero-byte file")
    with Image.open(path) as image:
        if image.format != "WEBP" or getattr(image, "n_frames", 1) != 1:
            raise ValidationError("Image is not a single-frame WebP")
        image.load()
        stats = check_pixels(image, production=True)
    if not is_lossless_webp(path):
        raise ValidationError("WebP is not lossless")
    return {**stats, "sizeBytes": path.stat().st_size, "lossless": True}


def convert_image(encoded, asset):
    from PIL import Image, ImageChops
    path = destination(asset)
    path.parent.mkdir(parents=True, exist_ok=True)
    # Source bytes stay in memory; no base64 image or API response is logged.
    source = base64.b64decode(encoded, validate=True)
    with Image.open(io.BytesIO(source)) as image:
        image.load()
        rgba = image.convert("RGBA")
        rgba = normalize_canvas(rgba)
    fd, name = tempfile.mkstemp(prefix=".trace-", suffix=".webp.tmp", dir=path.parent)
    os.close(fd)
    temporary = Path(name)
    try:
        rgba.save(temporary, "WEBP", lossless=True, quality=100, method=6, exact=True)
        validate_webp(temporary)
        with Image.open(temporary) as decoded:
            if ImageChops.difference(rgba, decoded.convert("RGBA")).getbbox(alpha_only=False):
                raise ValidationError("Lossless WebP changed source RGBA pixels")
        # Only replace the production asset after the candidate passes all checks.
        os.replace(temporary, path)
        return validate_webp(path, asset)
    finally:
        temporary.unlink(missing_ok=True)


def safe_error(error):
    # Never format arbitrary SDK exception bodies: authentication errors may echo
    # part of a key. Diagnostics use only a fixed class/status allowlist.
    if isinstance(error, ValidationError):
        return str(error)
    names = {"AuthenticationError": "API authentication failed",
             "PermissionDeniedError": "API permission denied",
             "NotFoundError": "API model or endpoint not found",
             "RateLimitError": "API rate limit or quota exceeded",
             "BadRequestError": "API rejected generation parameters or prompt",
             "APIConnectionError": "API connection failed",
             "APITimeoutError": "API request timed out",
             "InternalServerError": "API server error"}
    reason = names.get(type(error).__name__, "Generation, decoding, or file operation failed")
    status = getattr(error, "status_code", None)
    return f"{reason} (HTTP {status})" if type(status) is int else reason


def android_record(asset):
    return {"id": asset["id"], "displayName": asset["displayName"],
            "name": asset["displayName"],
            "category": ANDROID_CATEGORIES[asset["category"]],
            "categoryDirectory": asset["category"], "difficulty": asset["difficulty"],
            "featured": asset["featured"], "filename": asset["outputFilename"],
            "imagePath": asset["androidAssetPath"],
            "shortDescription": asset["shortDescription"],
            "description": asset["shortDescription"]}


def asset_prompt(asset, correction=None):
    # Identity context is essential: several verbatim descriptions specify only
    # a pose/accessory, e.g. "head tilted, one paw lifted".
    prompt = (GLOBAL_STYLE_PROMPT + "\n\nAnimal identity: " + asset["displayName"] +
              "; category: " + asset["category"] + ".\nSubject: " +
              asset["generationDescription"] + "\nDifficulty: " + asset["difficulty"] +
              " — " + DIFFICULTY_REQUIREMENTS[asset["difficulty"].upper()])
    if correction:
        prompt += "\nQuality-control correction: " + correction
    return prompt


def final_audit(assets):
    valid, invalid = [], []
    for asset in assets:
        try:
            stats = validate_webp(destination(asset), asset)
            valid.append((asset, stats))
        except Exception as error:
            invalid.append({"id": asset["id"], "path": asset["androidAssetPath"],
                            "reason": safe_error(error)})
    files = list((ASSETS / "templates").rglob("*.webp"))
    counts = Counter(p.name for p in files)
    expected_paths = {destination(a) for a in assets}
    return valid, {"validTotal": len(valid), "invalidOrMissing": invalid,
                   "duplicateBasenames": sorted(n for n, count in counts.items() if count > 1),
                   "unexpectedWebpPaths": sorted(str(p.relative_to(ASSETS)) for p in files if p not in expected_paths),
                   "categoryCounts": dict(Counter(a["category"] for a, _ in valid)),
                   "totalSizeBytes": sum(s["sizeBytes"] for _, s in valid)}


def run(args, assets):
    # This legacy generator knows only the original specification. Do not let a
    # later run replace the expanded app manifest with its old 84-record list.
    if ANDROID_MANIFEST.exists() and not args.validate_only:
        current_ids = {a["id"] for a in json.loads(ANDROID_MANIFEST.read_text())}
        if current_ids - {a["id"] for a in assets}:
            raise ValidationError("Expanded library detected; use the preview generator --expand-plan workflow")
    from PIL import features
    if not features.check("webp"):
        raise ValidationError("Pillow has no WebP codec")
    prior = json.loads(REPORT.read_text()) if REPORT.exists() else {}
    previous = {r["id"]: r for r in prior.get("assets", [])}
    selected = [a for a in assets if (not args.asset or a["id"] in args.asset)
                and (not args.category or a["category"] == args.category)
                and (not args.retry_failed or previous.get(a["id"], {}).get("status") == "failed")]
    if not selected and not args.validate_only:
        raise ValidationError("No assets match the selected filters/report")
    states = {}
    for a in assets:
        states[a["id"]] = previous.get(a["id"], {
            **{k: a[k] for k in ("id", "outputFilename", "category", "displayName", "difficulty")},
            "status": "pending", "retryCount": 0, "attemptCount": 0,
            "finalPath": str(destination(a)), "dimensions": None,
            "transparencyDetected": False, "visualReview": "pending",
        }).copy()
        states[a["id"]]["retryCount"] = max(0, states[a["id"]].get("attemptCount", 0) - 1)
    run_id = datetime.now(timezone.utc).isoformat()
    history = prior.get("runHistory", [])
    if prior.get("runId"):
        history = history + [{k: prior.get(k) for k in
                   ("runId", "currentRun", "model", "quality", "maxRetries", "concurrency")}]
    lock = threading.Lock()
    totals = Counter()

    def save_report(audit=None):
        data = {"expectedTotal": 84, "successfulTotal": sum(r["status"] == "success" for r in states.values()),
                "skippedTotal": sum(r["status"] == "skipped" for r in states.values()),
                "failedTotal": sum(r["status"] == "failed" for r in states.values()),
                "pendingTotal": sum(r["status"] in ("pending", "generating", "retrying") for r in states.values()),
                "retryTotal": sum(r.get("retryCount", 0) for r in states.values()),
                "model": MODEL, "quality": args.quality, "imageSize": SIZE,
                "conversionFormat": "lossless WebP", "background": "transparent",
                "maxRetries": args.max_retries, "concurrency": args.concurrency,
                "runId": run_id, "currentRun": dict(totals),
                "runHistory": history,
                "requestAttemptTotal": sum(r.get("attemptCount", 0) for r in states.values()),
                "updatedAt": datetime.now(timezone.utc).isoformat(),
                "templateManifestNote": "Validated files only; the existing drawable-based Android catalog does not load this JSON.",
                "assets": [states[a["id"]] for a in assets]}
        if audit is not None:
            data["verification"] = audit
        atomic_json(REPORT, data)

    if args.validate_only:
        valid, audit = final_audit(assets)
        print(json.dumps(audit, indent=2))
        return 0 if len(valid) == 84 and not audit["duplicateBasenames"] and not audit["unexpectedWebpPaths"] else 1

    if getattr(args, "normalize_existing", False):
        from PIL import Image
        for asset in selected:
            path = destination(asset)
            if not path.is_file():
                continue
            try:
                validate_webp(path, asset)
                continue
            except Exception:
                pass
            try:
                with Image.open(path) as image:
                    image.load()
                    if image.format != "WEBP" or not is_lossless_webp(path):
                        raise ValidationError("Existing file is not a lossless WebP")
                    source = io.BytesIO()
                    image.convert("RGBA").save(source, "PNG")
                stats = convert_image(base64.b64encode(source.getvalue()).decode(), asset)
                states[asset["id"]].update(stats, status="success", error=None,
                    canvasNormalized=True, visualReview="pending")
                totals["normalized"] += 1
            except Exception as error:
                states[asset["id"]].update(status="failed", error=safe_error(error))
            save_report()
        valid, audit = final_audit(assets)
        atomic_json(ANDROID_MANIFEST, [android_record(a) for a, _ in valid])
        save_report(audit)
        print(f"Normalized {totals['normalized']} existing assets; {len(valid)}/84 valid. No API requests.")
        return 0 if len(valid) == 84 else 1

    if not os.environ.get("OPENAI_API_KEY"):
        raise ValidationError("OPENAI_API_KEY is not available")
    # Suppress optional SDK/environment debug logging before creating the client.
    logging.disable(logging.CRITICAL)
    from openai import OpenAI
    client = OpenAI(base_url="https://api.openai.com/v1", max_retries=0, timeout=args.timeout)

    def progress(asset, status, **values):
        with lock:
            states[asset["id"]].update(status=status, **values)
            save_report()
            print(f"{asset['id']}: {status}" + (f" — {values['error']}" if values.get("error") else ""), flush=True)

    def worker(asset):
        state = states[asset["id"]]
        if not args.force:
            try:
                stats = validate_webp(destination(asset), asset)
            except Exception:
                pass
            else:
                with lock:
                    totals["skipped"] += 1
                progress(asset, "skipped", **stats, error=None)
                return
        previous_attempts = state.get("attemptCount", 0)
        remaining_attempts = (args.max_retries + 1 if args.force or args.retry_failed else
                              max(0, args.max_retries + 1 - previous_attempts))
        state.update(error=None, visualReview="pending",
                     model=MODEL, quality=args.quality)
        if not remaining_attempts:
            progress(asset, "failed", error="Configured attempt limit already exhausted", retryLimitExhausted=True)
            return
        for attempt in range(remaining_attempts):
            if (ROOT / "scripts/.imagegen-stop").exists():
                progress(asset, "pending")
                return
            progress(asset, "generating", retryCount=previous_attempts + attempt,
                     attemptCount=previous_attempts + attempt + 1)
            try:
                result = client.images.generate(
                    model=MODEL, prompt=asset_prompt(asset, getattr(args, "qc_note", None)),
                    size=SIZE, quality=args.quality, background="transparent", output_format="png", n=1,
                )
                if not result.data or len(result.data) != 1 or not result.data[0].b64_json:
                    raise ValidationError("API did not return exactly one base64 image")
                stats = convert_image(result.data[0].b64_json, asset)
                with lock:
                    totals["successful"] += 1
                progress(asset, "success", **stats, error=None, retryLimitExhausted=False)
                return
            except Exception as error:
                reason = safe_error(error)
                if attempt >= remaining_attempts - 1:
                    with lock:
                        totals["failed"] += 1
                    progress(asset, "failed", error=reason, retryLimitExhausted=True)
                    return
                delay = min(60.0, args.backoff * 2 ** attempt) + random.uniform(0, 1)
                response = getattr(error, "response", None)
                if response is not None:
                    retry_after = response.headers.get("retry-after", "")
                    try:
                        delay = max(delay, min(300.0, float(retry_after)))
                    except ValueError:
                        pass
                with lock:
                    totals["retries"] += 1
                progress(asset, "retrying", error=reason)
                time.sleep(delay)

    save_report()
    print(f"Verified 84 records. Processing {len(selected)}; model={MODEL}; concurrency={args.concurrency}; maxRetries={args.max_retries}", flush=True)
    try:
        with ThreadPoolExecutor(max_workers=args.concurrency) as executor:
            futures = [executor.submit(worker, asset) for asset in selected]
            for future in as_completed(futures):
                future.result()
    finally:
        client.close()
    valid, audit = final_audit(assets)
    # Failed/missing files are excluded from Android metadata, so it never points
    # at files that do not exist. The tooling manifest always retains all 84.
    atomic_json(ANDROID_MANIFEST, [android_record(a) for a, _ in valid])
    save_report(audit)
    print(f"Finished: {len(valid)}/84 valid; current run {dict(totals)}; {audit['totalSizeBytes']} bytes", flush=True)
    return 0 if len(valid) == 84 and not audit["duplicateBasenames"] and not audit["unexpectedWebpPaths"] else 1


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--asset", action="append", help="Exact asset ID; may be repeated")
    parser.add_argument("--category", choices=[v[0] for v in CATEGORIES.values()])
    parser.add_argument("--concurrency", type=int, default=3)
    parser.add_argument("--quality", choices=["low", "medium", "high", "xhigh", "max", "auto"], default="medium")
    parser.add_argument("--max-retries", type=int, default=3, help="Retries after the initial attempt")
    parser.add_argument("--timeout", type=float, default=240)
    parser.add_argument("--backoff", type=float, default=5)
    parser.add_argument("--force", action="store_true")
    parser.add_argument("--retry-failed", action="store_true")
    parser.add_argument("--prepare", action="store_true", help="Write/verify tooling manifest without API calls")
    parser.add_argument("--validate-only", action="store_true", help="Audit all outputs without API calls or mutations")
    parser.add_argument("--normalize-existing", action="store_true", help="Correct canvas margins on existing WebPs without API calls")
    parser.add_argument("--qc-note", help="Additional correction for a visually rejected asset; preserves the global style and exact description")
    args = parser.parse_args()
    if not 1 <= args.concurrency <= 16 or not 0 <= args.max_retries <= 10 or args.timeout <= 0 or args.backoff < 0:
        parser.error("Invalid concurrency, retries, timeout, or backoff")
    assets = parse_spec(SPEC.read_text(encoding="utf-8"))
    if args.asset and not set(args.asset) <= {a["id"] for a in assets}:
        parser.error("Unknown asset ID")
    if args.prepare:
        atomic_json(MANIFEST, assets)
        print("Specification and manifest verified: 84 unique assets; " +
              str(dict(Counter(a["category"] for a in assets))))
        return 0
    if not MANIFEST.exists() or json.loads(MANIFEST.read_text()) != assets:
        raise ValidationError("Tooling manifest differs from specification; run --prepare first")
    return run(args, assets)


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except KeyboardInterrupt:
        print("Interrupted. Valid files and the latest report are saved; rerun to resume.", file=sys.stderr)
        raise SystemExit(130)
    except Exception as error:
        print("Stopped: " + safe_error(error), file=sys.stderr)
        raise SystemExit(2)
