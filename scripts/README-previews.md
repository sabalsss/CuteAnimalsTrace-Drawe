# Color preview assets

Run from the project root with the existing image-generation environment:

```sh
scripts/.venv-imagegen/bin/python scripts/generate_preview_assets.py
```

The script reads `OPENAI_API_KEY` from the environment without printing it. It
edits each existing trace reference with `gpt-image-2.5-flare`, medium quality,
1024×1024, transparent background. Returned PNG pixels are converted to lossless
WebP in `app/src/main/assets/templates_preview/`. Trace files are read-only inputs.

Three concurrent requests run by default, with bounded retries. Valid previews
are skipped on restart. Progress and original trace fingerprints are checkpointed
in `scripts/preview_generation_report.json`. Ctrl-C stops scheduling more work;
rerun the same command to resume. `--asset ID` and `--category cats` select a subset.
Use `--force --asset ID --qc-note 'correction'` only for a preview that fails visual
review. Never run the trace generator to produce previews.

Validation checks exact dimensions, transparency, margins, color, lossless encoding,
and approximate registration with the source outline. Review pose, expression,
orientation, and accessories visually as well. The manifest gains only
`previewImagePath` and `traceImagePath` once all 84 previews pass validation.

```sh
scripts/.venv-imagegen/bin/python scripts/generate_preview_assets.py --validate-only
scripts/.venv-imagegen/bin/python -m unittest discover -s scripts -p test_preview_assets.py
```

Browsing/detail screens load previews; the tracing screen and overlay load the
original trace path. The application makes no image-generation API calls.
