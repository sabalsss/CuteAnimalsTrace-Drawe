# Production tracing assets

Python tooling is separate from Android/Gradle. Requires Python 3.10+ and an
`OPENAI_API_KEY` in the process environment. Never paste a key into a command,
source file, JSON, or log. The generator suppresses SDK logging and reports only
fixed error descriptions, because API error messages can contain credentials.

```sh
python3.12 -m venv scripts/.venv-imagegen
scripts/.venv-imagegen/bin/python -m pip install --upgrade -r scripts/requirements-imagegen.txt
scripts/.venv-imagegen/bin/python scripts/generate_trace_assets.py --prepare
scripts/.venv-imagegen/bin/python scripts/generate_trace_assets.py
```

Default: `gpt-image-2.5-flare`, medium quality, 1024×1024, transparent PNG source,
one image per request, three workers, three retries after the initial attempt,
240-second request timeout. SDK retries are disabled so retry counts are exact.
Backoff starts at 5 seconds, doubles, adds jitter, and honors Retry-After up to
300 seconds. Each failed asset continues independently through its retry budget.

Options: `--asset cat_001` (repeatable), `--category cats`, `--concurrency 3`,
`--quality medium`, `--force`, `--retry-failed`, `--max-retries 3`, `--timeout 240`,
`--backoff 5`, `--prepare`, `--validate-only`. Valid existing WebPs are skipped
unless `--force` is supplied. A failed replacement never overwrites a valid file.

The tooling manifest is derived from the specification and must match it exactly
before any billed request. Source images are decoded in memory, converted with
Pillow lossless WebP, checked for exact RGBA round-trip preservation, and moved
atomically into the production directory only after validation. If the API ignores
layout instructions, conversion uniformly scales and centers the complete subject
at 72% of the transparent canvas; it does not redraw any artwork. Encoding is
lossless relative to the resulting RGBA pixels. `--normalize-existing` applies
this layout correction to existing out-of-range images without any API calls.
Temporary files
are removed. The validator checks WebP lossless encoding, transparency, dimensions,
nonempty ink, basic margins, color and excessive gray shading. These heuristics do
not establish anatomical correctness, originality, or visual consistency: inspect
every asset at phone size and 40–60% opacity, with extra attention to featured art.
The report starts `visualReview` at `pending` and must not be interpreted as human
visual approval. Regenerate a rejected asset using `--asset ID --force`, optionally
with `--qc-note "Specific correction"`. Each prompt includes the asset display name
and category to disambiguate short descriptions that omit the animal species,
alongside the unchanged global style, verbatim description, and difficulty.
Create `scripts/.imagegen-stop` to stop new requests after active calls finish;
remove it before resuming. Interrupted attempts count toward the original budget.
An explicit `--force` or `--retry-failed` starts another configurable attempt budget.

`generation_report.json` is checkpointed on each state transition and includes all
84 records. Totals describe the latest stored state for all records; `currentRun`
describes this invocation. `--retry-failed` selects records marked `failed` in the
saved report. `--validate-only` audits the full library and makes no mutations.

`app/src/main/assets/templates_manifest.json` contains only validated assets, so
partial runs never create dangling image paths. All paths are relative to Android
assets and end in `.webp`; category/difficulty values match the existing Kotlin
enums. `name`/`description` aliases accompany `displayName`/`shortDescription`.
The current app uses `DrawingTemplate.imageRes` and a compiled drawable catalog;
it does **not** consume this JSON. An asset loader is a separate app milestone.

API references:
- https://developers.openai.com/api/docs/models/gpt-image-2.5-flare
- https://developers.openai.com/api/docs/guides/image-generation
