#!/usr/bin/env python3
"""Colorize existing trace references without ever writing into templates/.

Uses the project's existing SDK environment, safe diagnostics, atomic JSON writer,
and lossless-WebP checker. Preview generation never invokes trace generation.
"""
from __future__ import annotations
import argparse
import base64
from collections import Counter
from concurrent.futures import ThreadPoolExecutor, as_completed
from datetime import datetime, timezone
import hashlib
import io
import json
import logging
import os
from pathlib import Path
import random
import signal
import tempfile
import threading
import time

from PIL import Image, ImageChops, ImageFilter
from generate_trace_assets import atomic_json, is_lossless_webp, safe_error, ValidationError

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "app/src/main/assets"
MANIFEST = ASSETS / "templates_manifest.json"
REPORT = ROOT / "scripts/preview_generation_report.json"
MODEL = "gpt-image-2.5-flare"
EXPECTED_COUNTS = dict(cats=15, dogs=15, bunnies=10, pandas=8, foxes=8, bears=8, kawaii=10, baby_animals=10)
GLOBAL_PREVIEW_STYLE_PROMPT = """Colorize the provided black-outline tracing illustration.
The reference image is the authoritative drawing: preserve exactly the same animal,
silhouette, pose, expression, head angle, eye direction, orientation, limb positions,
tail shape, ear shapes, accessories and their placement, and overall composition.
Add color inside its existing outlines. Do not redesign, mirror, rotate, or invent
a different pose. Keep the original line structure and its friendly expression.
Use one coherent premium mobile-app style: soft pastel colors, clean dark outlines,
simple flat fills, subtle blush pink cheeks where appropriate, and at most very
light minimal shading. Prefer mint, peach, blush pink, cream, pale yellow, light
tan, warm brown, soft gray and soft orange. Keep small details readable in a card.
Use a genuinely transparent background, no white rectangle or checkerboard.
Keep the same centered placement and generous margins as the reference, roughly
65–75% canvas occupancy. Preserve every ear, paw, tail, horn, fin, wing, accessory.
No additional animal or objects. No scenery, photorealism, heavy shadows, harsh
gradients, excessive detail, text, letters, logos, watermarks, borders or frames.
Do not introduce resemblance to copyrighted or branded characters.
Output one 1024x1024 square colored illustration, not a comparison or collage.
"""
PALETTES = {
    "cats": "warm cream and soft peach fur; blush cheeks; existing markings light tan",
    "dogs": "light tan and warm cream fur; warm brown ears; blush cheeks",
    "bunnies": "warm cream fur; blush pink inner ears and cheeks",
    "pandas": "warm cream and soft charcoal-gray patches; tiny blush pink cheeks; mint bamboo if present",
    "foxes": "soft orange fur and cream muzzle, chest and tail tip; blush cheeks",
    "bears": "light tan fur and warm cream muzzle; blush cheeks; mint or peach accessories if present",
    "kawaii": "gentle natural species colors interpreted in warm pastel tones; blush cheeks",
    "baby_animals": "gentle natural species colors interpreted in warm pastel tones; blush cheeks",
}


def digest(path):
    """Fingerprint an image file, never credentials."""
    return hashlib.sha256(path.read_bytes()).hexdigest()


def load_library():
    records = json.loads(MANIFEST.read_text())
    if len(records) < 84 or len({a['id'] for a in records}) != len(records):
        raise ValidationError("Manifest must retain the original library and unique template IDs")
    result = []
    for original in records:
        asset = dict(original)
        trace = asset.get("traceImagePath", asset.get("imagePath", ""))
        parts = Path(trace).parts
        if len(parts) != 3 or parts[0] != "templates" or parts[1] not in EXPECTED_COUNTS or parts[2] != asset['id'] + ".webp":
            raise ValidationError("Trace path/ID/category mismatch")
        if not (ASSETS/trace).resolve().is_relative_to((ASSETS/'templates').resolve()):
            raise ValidationError("Unsafe trace path")
        with Image.open(ASSETS/trace) as image:
            image.load()
            if image.format != "WEBP" or image.size != (1024, 1024):
                raise ValidationError("Missing or corrupt trace reference")
        asset.update(traceImagePath=trace, previewImagePath=f"templates_preview/{parts[1]}/{asset['id']}_preview.webp",
                     categoryDirectory=parts[1])
        result.append(asset)
    counts=Counter(a['categoryDirectory'] for a in result)
    if any(counts[category] < minimum for category,minimum in EXPECTED_COUNTS.items()):
        raise ValidationError("Manifest is missing original category content")
    return records, result


def preview_path(asset):
    expected = f"templates_preview/{asset['categoryDirectory']}/{asset['id']}_preview.webp"
    if asset['previewImagePath'] != expected:
        raise ValidationError("Invalid preview destination")
    path = ASSETS / expected
    if not path.resolve().is_relative_to((ASSETS/'templates_preview').resolve()):
        raise ValidationError("Preview path escapes preview directory")
    return path


def on_white(image):
    canvas = Image.new('RGBA', image.size, 'white')
    canvas.alpha_composite(image.convert('RGBA'))
    return canvas.convert('RGB')


def ink_mask(image, threshold=120):
    return on_white(image).convert('L').point(lambda p: 255 if p < threshold else 0)


def reference(asset):
    with Image.open(ASSETS/asset['traceImagePath']) as image:
        return image.convert('RGBA')


def align_preview(image, trace):
    """Uniform layout correction only; never change the input trace image."""
    if image.size != (1024,1024):
        raise ValidationError('Preview dimensions are not 1024x1024')
    rgba = image.convert('RGBA')
    if rgba.getchannel('A').getextrema()[0] == 255:
        raise ValidationError('Preview lacks a transparent background')
    box = rgba.getchannel('A').point(lambda p: 255 if p > 32 else 0).getbbox()
    target = ink_mask(trace).getbbox()
    if box is None or target is None:
        raise ValidationError('Empty preview or trace')
    x0,y0,x1,y1 = box
    if min(x0,y0,1024-x1,1024-y1)<2:
        raise ValidationError('Preview is cropped or has a full-canvas background')
    width,height = x1-x0,y1-y0
    tw,th = target[2]-target[0],target[3]-target[1]
    if not 0.8 < (width/height)/(tw/th) < 1.25:
        raise ValidationError('Preview silhouette aspect differs from trace')
    # Keep antialiased edges, but discard the empty transparent perimeter.
    box=(max(0,x0-2),max(0,y0-2),min(1024,x1+2),min(1024,y1+2))
    cropped=rgba.crop(box)
    scale=min(tw/width,th/height)
    resized=cropped.resize((round(cropped.width*scale),round(cropped.height*scale)),Image.Resampling.LANCZOS)
    canvas=Image.new('RGBA',(1024,1024),(0,0,0,0))
    x=round((target[0]+target[2]-resized.width)/2)
    y=round((target[1]+target[3]-resized.height)/2)
    canvas.alpha_composite(resized,(x,y))
    return canvas


def image_stats(image, trace):
    if image.size != (1024,1024):
        raise ValidationError('Incorrect preview dimensions')
    rgba=image.convert('RGBA');alpha=rgba.getchannel('A')
    if alpha.getextrema()[0] != 0 or alpha.getextrema()[1] < 200:
        raise ValidationError('Missing transparent background or visible subject')
    box=alpha.point(lambda p:255 if p>32 else 0).getbbox()
    if box is None or min(box[0],box[1],1024-box[2],1024-box[3])<70:
        raise ValidationError('Preview lacks generous margins')
    occupancy=max(box[2]-box[0],box[3]-box[1])/1024
    if not .63<=occupancy<=.77:
        raise ValidationError('Preview canvas occupancy outside expected range')
    rgb=on_white(rgba);r,g,b=rgb.split()
    chroma=ImageChops.lighter(ImageChops.difference(r,g),ImageChops.difference(g,b))
    color_pixels=sum(chroma.histogram()[20:])
    if color_pixels<1000:
        raise ValidationError('Preview is not colorful')
    # An inexpensive registration guard. Visual review still checks animal,
    # expression and accessories; this score is not proof of anatomical fidelity.
    ref=ink_mask(trace).resize((256,256)).point(lambda p:255 if p>80 else 0)
    out=ink_mask(rgba,135).resize((256,256)).point(lambda p:255 if p>80 else 0)
    nearby=out.filter(ImageFilter.MaxFilter(9))
    count=ref.histogram()[255]
    overlap=ImageChops.multiply(ref,nearby).histogram()[255]/max(count,1)
    if overlap<.72:
        raise ValidationError('Preview outline alignment differs from trace')
    return dict(dimensions=[1024,1024],transparencyDetected=True,colorPixels=color_pixels,
                canvasOccupancy=round(occupancy,4),referenceOutlineRecall=round(overlap,4))


def validate_preview(path, asset):
    if not path.is_file() or not path.stat().st_size:
        raise ValidationError('Missing or empty preview')
    with Image.open(path) as image:
        image.load()
        if image.format!='WEBP' or getattr(image,'n_frames',1)!=1 or not is_lossless_webp(path):
            raise ValidationError('Preview is not a single-frame lossless WebP')
        stats=image_stats(image,reference(asset))
    return dict(stats,sizeBytes=path.stat().st_size,lossless=True)


def save_preview(encoded, asset):
    path=preview_path(asset)
    with Image.open(io.BytesIO(base64.b64decode(encoded,validate=True))) as source:
        source.load();rgba=align_preview(source,reference(asset))
    image_stats(rgba,reference(asset))
    path.parent.mkdir(parents=True,exist_ok=True)
    fd,name=tempfile.mkstemp(prefix='.preview-',suffix='.tmp',dir=path.parent);os.close(fd)
    temp=Path(name)
    try:
        rgba.save(temp,'WEBP',lossless=True,quality=100,method=6,exact=True)
        stats=validate_preview(temp,asset)
        with Image.open(temp) as decoded:
            if ImageChops.difference(rgba,decoded.convert('RGBA')).getbbox(alpha_only=False):
                raise ValidationError('Lossless preview round trip altered pixels')
        os.replace(temp,path)
        return stats
    finally:
        temp.unlink(missing_ok=True)


def audit(assets):
    valid=[];failed=[]
    for a in assets:
        try:
            validate_preview(preview_path(a),a);valid.append(a['id'])
        except Exception as error:
            failed.append(dict(id=a['id'],reason=safe_error(error)))
    actual=list((ASSETS/'templates_preview').rglob('*.webp'))
    expected={preview_path(a) for a in assets}
    return dict(validTotal=len(valid),failures=failed,
                unexpectedFiles=[str(p.relative_to(ASSETS)) for p in actual if p not in expected],
                duplicateBasenames=[n for n,c in Counter(p.name for p in actual).items() if c>1])


def update_manifest(original, assets):
    # Update only the two image-role fields; preserve every existing metadata value.
    current=json.loads(MANIFEST.read_text())
    if current!=original:
        raise ValidationError('Manifest changed during generation; refusing to overwrite concurrent edits')
    by_id={a['id']:a for a in assets}
    merged=[dict(a,traceImagePath=by_id[a['id']]['traceImagePath'],
                 previewImagePath=by_id[a['id']]['previewImagePath']) for a in original]
    atomic_json(MANIFEST,merged)



def expand_library(plan_path, budget=5.0, limit=None):
    """Append new trace/preview pairs, preserving existing records and files.

    Sequential requests keep budget reservations and checkpoints deterministic.
    The old 84-template workflow is intentionally not called here.
    """
    from generate_trace_assets import convert_image, validate_webp, android_record
    from openai import OpenAI
    records=json.loads(Path(plan_path).read_text())
    report_path=ROOT/'scripts/animal_expansion_report.json'
    report=json.loads(report_path.read_text()) if report_path.exists() else dict(
        budgetUSD=budget, model=MODEL, requests=[], completed=[], originalManifest=json.loads(MANIFEST.read_text()),
        originalImages={str(p.relative_to(ASSETS)):digest(p) for p in ASSETS.rglob('*.webp')})
    if report['budgetUSD']!=budget:raise ValidationError('Budget changed from checkpoint')
    original_ids={a['id'] for a in report['originalManifest']}
    if len({a['id'] for a in records})!=len(records) or any(a['id'] in original_ids for a in records):
        raise ValidationError('Expansion must use unique new IDs')
    def protect():
        if any(digest(ASSETS/p)!=v for p,v in report['originalImages'].items()):
            raise ValidationError('An original asset changed')
        current={a['id']:a for a in json.loads(MANIFEST.read_text())}
        if any(current.get(a['id'])!=a for a in report['originalManifest']):
            raise ValidationError('Original metadata changed')
    def save():atomic_json(report_path,report)
    def spent():return sum(r.get('costUSD',r['reservedUSD']) for r in report['requests'])
    protect();save()
    if not os.environ.get('OPENAI_API_KEY'):raise ValidationError('OPENAI_API_KEY is not available')
    logging.disable(logging.CRITICAL)
    client=OpenAI(base_url='https://api.openai.com/v1',max_retries=0,timeout=240)
    def request(kind, asset, prompt, image=None):
        # Reserve $0.50 before each request, including ambiguous interrupted calls.
        # Stop well before the budget, using actual usage as soon as it is returned.
        if spent()+0.50>budget:raise ValidationError('Budget reserve reached; stopping before another request')
        row=dict(id=asset['id'],kind=kind,reservedUSD=0.50,status='pending')
        report['requests'].append(row);save()
        try:
            kwargs=dict(model=MODEL,prompt=prompt,size='1024x1024',quality='medium',background='transparent',output_format='png',n=1)
            result=client.images.edit(image=image,**kwargs) if image else client.images.generate(**kwargs)
            usage=result.usage
            if usage is None:raise ValidationError('Usage missing; budget cannot be measured')
            details=usage.input_tokens_details
            text_tokens=details.text_tokens;image_tokens=details.image_tokens
            output_tokens=usage.output_tokens
            # Uncached rates are conservative if the service applies caching.
            cost=(text_tokens*5+image_tokens*8+output_tokens*30)/1_000_000
            row.update(status='returned',textInputTokens=text_tokens,imageInputTokens=image_tokens,
                       imageOutputTokens=output_tokens,costUSD=cost)
            save()
            if spent()>budget:raise ValidationError('Budget reached')
            if not result.data or len(result.data)!=1 or not result.data[0].b64_json:
                raise ValidationError('API did not return exactly one image')
            return result.data[0].b64_json
        except Exception as error:
            row.update(status='failed',error=safe_error(error));save();raise
    trace_style="""Use case: illustration-story. Create one cute kawaii animal black-outline tracing template for a children's drawing app. Clean smooth bold black outlines only, simple closed shapes, friendly face with small dark eyes. NO color, shading, gray fills, shadows, scene, ground line, text, logos, borders or watermark. Animal body interiors must remain transparent, not opaque white. Actual transparent background. One animal, whole body centered with every appendage visible. Subject fills 70% of a 1024x1024 square with generous blank margins. Simple premium mobile-app line art, easy to trace on paper. No accessory unless explicitly requested. Subject: """
    try:
        for a in records[:limit]:
            protect()
            trace=ASSETS/a['traceImagePath'];preview=preview_path(a)
            ta=dict(a,category=a['categoryDirectory'],androidAssetPath=a['traceImagePath'],outputFilename=a['id']+'.webp')
            if not trace.exists():
                print(a['id']+': trace generating',flush=True)
                for attempt in range(3):
                    encoded=request('trace',a,trace_style+a['description'])
                    try:convert_image(encoded,ta);break
                    except ValidationError:
                        if attempt==2:raise
            validate_webp(trace)
            if not preview.exists():
                print(a['id']+': preview generating',flush=True)
                buffer=io.BytesIO();on_white(reference(a)).save(buffer,'PNG')
                for attempt in range(3):
                    encoded=request('preview',a,GLOBAL_PREVIEW_STYLE_PROMPT+'\nAnimal: '+a['displayName']+'. '+a['description']+'\nPalette: '+a['palette'],
                                    (a['id']+'.png',buffer.getvalue(),'image/png'))
                    try:save_preview(encoded,a);break
                    except ValidationError:
                        if attempt==2:raise
            validate_preview(preview,a)
            if a['id'] not in report['completed']:report['completed'].append(a['id'])
            report['estimatedCostUSD']=round(spent(),6);save()
            print(a['id']+': pair ready; cost so far $'+format(spent(),'.4f'),flush=True)
        protect()
        report['estimatedCostUSD']=round(spent(),6);save()
        print('Ready for visual review: '+str(len(report['completed']))+' pairs. Manifest not changed yet.',flush=True)
    finally:client.close()


def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--expand-plan')
    parser.add_argument('--budget-usd',type=float,default=5.0)
    parser.add_argument('--limit',type=int)
    parser.add_argument('--asset',action='append')
    parser.add_argument('--category',choices=list(EXPECTED_COUNTS))
    parser.add_argument('--concurrency',type=int,default=3)
    parser.add_argument('--max-retries',type=int,default=3)
    parser.add_argument('--quality',default='medium',choices=['low','medium','high'])
    parser.add_argument('--timeout',type=float,default=240)
    parser.add_argument('--force',action='store_true')
    parser.add_argument('--retry-failed',action='store_true')
    parser.add_argument('--validate-only',action='store_true')
    parser.add_argument('--qc-note',default='')
    args=parser.parse_args()
    if not 1<=args.concurrency<=8 or not 0<=args.max_retries<=10 or args.timeout<=0:
        raise ValidationError('Invalid concurrency, retries or timeout')
    if args.expand_plan:
        expand_library(args.expand_plan,args.budget_usd,args.limit)
        return 0
    original,assets=load_library()
    if args.asset and not set(args.asset)<={a['id'] for a in assets}:
        raise ValidationError('Unknown asset ID')
    fingerprints={a['traceImagePath']:digest(ASSETS/a['traceImagePath']) for a in assets}
    prior=json.loads(REPORT.read_text()) if REPORT.exists() else {}
    if any(fingerprints.get(path)!=value for path,value in prior.get('traceFingerprints',fingerprints).items()):
        raise ValidationError('Trace reference changed since the original preview run')
    if args.validate_only:
        result=audit(assets);print(json.dumps(result,indent=2));return 0 if result['validTotal']==len(assets) else 1
    if not os.environ.get('OPENAI_API_KEY'):
        raise ValidationError('OPENAI_API_KEY is not available')
    logging.disable(logging.CRITICAL)
    from openai import OpenAI
    client=OpenAI(base_url='https://api.openai.com/v1',max_retries=0,
                  timeout=args.timeout)
    old={a['id']:a for a in prior.get('assets',[])}
    states={a['id']:dict(old.get(a['id'],{}),id=a['id'],previewImagePath=a['previewImagePath'],
                         traceImagePath=a['traceImagePath']) for a in assets}
    for state in states.values():
        state.setdefault('status','pending');state.setdefault('attemptCount',0)
    selected=[a for a in assets if (not args.asset or a['id'] in args.asset)
              and (not args.category or a['categoryDirectory']==args.category)
              and (not args.retry_failed or old.get(a['id'],{}).get('status')=='failed')]
    lock=threading.Lock();stop=threading.Event();totals=Counter()
    signal.signal(signal.SIGINT,lambda *_:stop.set())
    signal.signal(signal.SIGTERM,lambda *_:stop.set())

    def persist(verification=None,manifest_updated=False):
        report=dict(expectedTotal=len(assets),model=MODEL,quality=args.quality,size='1024x1024',
            conversionFormat='lossless WebP',concurrency=args.concurrency,maxRetries=args.max_retries,
            successfulTotal=sum(a['status']=='success' for a in states.values()),
            skippedTotal=sum(a['status']=='skipped' for a in states.values()),
            failedTotal=sum(a['status']=='failed' for a in states.values()),
            pendingTotal=sum(a['status'] in ('pending','generating','retrying') for a in states.values()),
            currentRun=dict(totals),traceFingerprints=fingerprints,
            updatedAt=datetime.now(timezone.utc).isoformat(),manifestUpdated=manifest_updated,
            assets=[states[a['id']] for a in assets])
        if verification is not None:report['verification']=verification
        atomic_json(REPORT,report)

    def status(a,state,**kwargs):
        with lock:
            states[a['id']].update(status=state,**kwargs);persist()
            print(a['id']+': '+state+(' — '+kwargs['error'] if kwargs.get('error') else ''),flush=True)

    def worker(a):
        if stop.is_set() or (ROOT/'scripts/.preview-stop').exists():return
        source=ASSETS/a['traceImagePath']
        if digest(source)!=fingerprints[a['traceImagePath']]:
            raise ValidationError('Trace reference changed during generation')
        if not args.force:
            try:stats=validate_preview(preview_path(a),a)
            except Exception:pass
            else:
                with lock:totals['skipped']+=1
                status(a,'skipped',**stats,error=None);return
        reference_png=io.BytesIO();on_white(reference(a)).save(reference_png,'PNG')
        prompt=(GLOBAL_PREVIEW_STYLE_PROMPT+'\nTemplate: '+a['displayName']+' ('+a['category']+').\n'
                'Existing description (reference image takes precedence for pose): '+a['shortDescription']+
                '\nPalette guidance: '+PALETTES[a['categoryDirectory']]+'.')
        if args.qc_note:prompt+='\nCorrection: '+args.qc_note
        attempts=states[a['id']]['attemptCount']
        for attempt in range(args.max_retries+1):
            if stop.is_set() or (ROOT/'scripts/.preview-stop').exists():return
            status(a,'generating',attemptCount=attempts+attempt+1,visualReview='pending')
            try:
                result=client.images.edit(model=MODEL,image=(a['id']+'.png',reference_png.getvalue(),'image/png'),
                    prompt=prompt,quality=args.quality,size='1024x1024',background='transparent',output_format='png',n=1)
                if not result.data or len(result.data)!=1 or not result.data[0].b64_json:
                    raise ValidationError('API did not return exactly one image')
                stats=save_preview(result.data[0].b64_json,a)
                if digest(source)!=fingerprints[a['traceImagePath']]:
                    raise ValidationError('Trace reference changed during generation')
                with lock:totals['generated']+=1
                status(a,'success',**stats,error=None);return
            except Exception as error:
                reason=safe_error(error)
                if attempt==args.max_retries:
                    with lock:totals['failed']+=1
                    status(a,'failed',error=reason);return
                delay=min(60,5*2**attempt)+random.random()
                response=getattr(error,'response',None)
                if response is not None:
                    try:delay=max(delay,min(300,float(response.headers.get('retry-after','0'))))
                    except ValueError:pass
                with lock:totals['retries']+=1
                status(a,'retrying',error=reason);stop.wait(delay)

    print(f'Verified {len(assets)} trace references. Processing {len(selected)} previews; concurrency={args.concurrency}.',flush=True)
    persist()
    try:
        with ThreadPoolExecutor(max_workers=args.concurrency) as executor:
            for future in as_completed([executor.submit(worker,a) for a in selected]):future.result()
    finally:client.close()
    if any(digest(ASSETS/p)!=value for p,value in fingerprints.items()):
        raise ValidationError('Original trace integrity check failed')
    verification=audit(assets)
    complete=verification['validTotal']==len(assets) and not verification['unexpectedFiles'] and not verification['duplicateBasenames']
    if complete:update_manifest(original,assets)
    persist(verification,complete)
    print(f'Finished: {verification["validTotal"]}/{len(assets)} valid previews; {dict(totals)}; original traces unchanged.',flush=True)
    return 0 if complete else 1


if __name__=='__main__':
    try:raise SystemExit(main())
    except Exception as error:
        print('Stopped: '+safe_error(error),flush=True);raise SystemExit(2)
