"""Offline tests: no API calls and no production asset writes."""
import argparse
import base64
import contextlib
import io
import json
from pathlib import Path
import tempfile
from types import SimpleNamespace
import unittest
from unittest.mock import patch

from PIL import Image, ImageDraw
import generate_trace_assets as gen


def drawing_bytes():
    image = Image.new("RGBA", (1024, 1024), (0, 0, 0, 0))
    ImageDraw.Draw(image).ellipse((150, 150, 874, 874), outline="black", width=12)
    buf = io.BytesIO()
    image.save(buf, "PNG")
    return buf.getvalue()


class GeneratorTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.spec = gen.SPEC.read_text()
        cls.records = gen.parse_spec(cls.spec)

    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.addCleanup(self.tmp.cleanup)
        root = Path(self.tmp.name)
        for name, value in {
            "ASSETS": root / "assets", "REPORT": root / "report.json",
            "ANDROID_MANIFEST": root / "assets/templates_manifest.json",
        }.items():
            patcher = patch.object(gen, name, value)
            patcher.start()
            self.addCleanup(patcher.stop)

    def test_spec_exact_counts_descriptions_and_featured(self):
        self.assertEqual(84, len(self.records))
        self.assertEqual(12, sum(a["featured"] for a in self.records))
        self.assertEqual("seated round kitten, front-facing, upright ears, curled tail, happy face",
                         self.records[0]["generationDescription"])
        self.assertEqual("BabyAnimals", gen.android_record(self.records[-1])["category"])

    def test_bad_spec_is_rejected_before_requests(self):
        for invalid in (self.spec.replace("cat_015.png —", "cat_014.png —"),
                        self.spec + "\nextra_001.png — Extra — Easy — extra\n",
                        self.spec.replace("## Cats — 15", "## Cats — 14")):
            with self.assertRaises(gen.ValidationError):
                gen.parse_spec(invalid)
        gen.parse_spec(self.spec)

    def test_lossless_rgba_conversion_and_validation(self):
        asset = self.records[0]
        stats = gen.convert_image(base64.b64encode(drawing_bytes()).decode(), asset)
        self.assertTrue(stats["transparencyDetected"])
        self.assertTrue(stats["lossless"])
        with Image.open(gen.destination(asset)) as decoded, Image.open(io.BytesIO(drawing_bytes())) as source:
            self.assertEqual(source.convert("RGBA").tobytes(), decoded.convert("RGBA").tobytes())
        self.assertFalse(list(gen.ASSETS.rglob("*.tmp")))

    def test_empty_opaque_wrong_size_and_color_rejected(self):
        for image in (Image.new("RGBA", (1024, 1024)),
                      Image.new("RGBA", (1024, 1024), "white"),
                      Image.new("RGBA", (512, 512))):
            with self.assertRaises(gen.ValidationError):
                gen.check_pixels(image)
        colored = Image.open(io.BytesIO(drawing_bytes())).convert("RGBA")
        ImageDraw.Draw(colored).ellipse((200, 200, 800, 800), outline="red", width=20)
        with self.assertRaises(gen.ValidationError):
            gen.check_pixels(colored)

    def test_failed_replacement_preserves_existing_valid_file(self):
        asset = self.records[0]
        gen.convert_image(base64.b64encode(drawing_bytes()).decode(), asset)
        before = gen.destination(asset).read_bytes()
        with self.assertRaises(Exception):
            gen.convert_image("invalid base64", asset)
        self.assertEqual(before, gen.destination(asset).read_bytes())

    def test_wrong_path_and_lossy_webp_rejected(self):
        asset = self.records[0]
        bad = dict(asset, androidAssetPath="templates/dogs/cat_001.webp")
        with self.assertRaises(gen.ValidationError):
            gen.destination(bad)
        path = gen.destination(asset)
        path.parent.mkdir(parents=True)
        Image.open(io.BytesIO(drawing_bytes())).save(path, "WEBP", lossless=False)
        with self.assertRaises(gen.ValidationError):
            gen.validate_webp(path, asset)

    def test_canvas_normalization_and_identity_prompt(self):
        image = Image.new("RGBA", (1024, 1024), (0, 0, 0, 0))
        ImageDraw.Draw(image).ellipse((50, 50, 960, 960), outline="black", width=12)
        normalized = gen.normalize_canvas(image)
        stats = gen.check_pixels(normalized, production=True)
        self.assertLessEqual(stats["canvasOccupancy"], .75)
        self.assertGreaterEqual(stats["canvasOccupancy"], .65)
        prompt = gen.asset_prompt(self.records[6])
        self.assertIn("Animal identity: Curious Kitten; category: cats", prompt)
        self.assertIn(self.records[6]["generationDescription"], prompt)

    def test_arbitrary_errors_never_reveal_exception_body(self):
        class AuthenticationError(Exception):
            status_code = 401
        error = AuthenticationError("sensitive exception body must never be logged")
        self.assertEqual("API authentication failed (HTTP 401)", gen.safe_error(error))
        self.assertNotIn("sensitive", gen.safe_error(RuntimeError("sensitive")))

    def test_retry_report_manifest_then_resume_skip(self):
        response = SimpleNamespace(data=[SimpleNamespace(b64_json=base64.b64encode(drawing_bytes()).decode())])
        args = argparse.Namespace(asset=["cat_001", "cat_002"], category=None,
            retry_failed=False, validate_only=False, force=False, concurrency=1,
            quality="medium", max_retries=1, timeout=30, backoff=0)
        # First asset fails once then succeeds; second exhausts both attempts.
        with patch("openai.OpenAI") as factory, patch.object(gen.time, "sleep"), \
                patch.dict(gen.os.environ, {"OPENAI_API_KEY": "offline-test-placeholder"}), \
                contextlib.redirect_stdout(io.StringIO()):
            factory.return_value.images.generate.side_effect = [
                RuntimeError("private"), response, RuntimeError("private"), RuntimeError("private")]
            self.assertEqual(1, gen.run(args, self.records))
            self.assertEqual(4, factory.return_value.images.generate.call_count)
            report = json.loads(gen.REPORT.read_text())
            self.assertEqual(1, report["successfulTotal"])
            self.assertEqual(1, report["failedTotal"])
            self.assertEqual(2, report["retryTotal"])
            self.assertEqual(1, len(json.loads(gen.ANDROID_MANIFEST.read_text())))
            self.assertNotIn("private", gen.REPORT.read_text())
            factory.return_value.images.generate.reset_mock()
            args.asset = ["cat_001"]
            gen.run(args, self.records)
            factory.return_value.images.generate.assert_not_called()
            report = json.loads(gen.REPORT.read_text())
            self.assertEqual("skipped", report["assets"][0]["status"])
            self.assertEqual("failed", report["assets"][1]["status"])


if __name__ == "__main__":
    unittest.main()
