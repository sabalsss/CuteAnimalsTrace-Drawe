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
`previewImagePath` and `traceImagePath` once all catalog previews pass validation.

```sh
scripts/.venv-imagegen/bin/python scripts/generate_preview_assets.py --validate-only
scripts/.venv-imagegen/bin/python -m unittest discover -s scripts -p test_preview_assets.py
```

Browsing/detail screens load previews; the tracing screen and overlay load the
original trace path. The application makes no image-generation API calls.

## Adding new animals within a budget

The new animal prompt set is `scripts/animal_expansion_manifest.json`. Each record
specifies the species, tracing pose, palette, and both destination paths. Existing
IDs and files are protected by the expansion report's metadata snapshot and image
fingerprints. The original trace generator refuses to overwrite an expanded catalog.

```sh
scripts/.venv-imagegen/bin/python scripts/generate_preview_assets.py \
  --expand-plan scripts/animal_expansion_manifest.json --budget-usd 5
```

This uses the established `gpt-image-2.5-flare` API workflow: a new transparent
black-outline trace, followed by a reference-based color edit. Both are normalized
to 1024×1024 and encoded losslessly. Requests run one at a time. Valid saved pairs
are reused on resume. The report records token usage at conservative uncached
rates ($5/M text input, $8/M image input, $30/M image output), including returned
images that need a retry. It reserves $0.50 before every call; interrupted requests
without usage retain that reservation. Generation stops if the next reservation
would exceed the budget. The local estimate is not a billing statement.

Review every pair before appending its record (without the generation-only palette)
to the Android manifest. The expansion command deliberately does not publish unreviewed
assets to the catalog. Keep the previous manifest records byte-for-byte equivalent
as JSON values. The usual `--validate-only` command supports the expanded catalog.
