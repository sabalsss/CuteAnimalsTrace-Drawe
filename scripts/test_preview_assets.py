"""Preview conversion/integrity tests; no API calls or production writes."""
import base64
import io
import json
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch
from PIL import Image, ImageDraw
import generate_preview_assets as gen


class PreviewAssetTests(unittest.TestCase):
    def setUp(self):
        self.temp=tempfile.TemporaryDirectory();self.addCleanup(self.temp.cleanup)
        self.root=Path(self.temp.name)
        for name,value in [('ASSETS',self.root),('MANIFEST',self.root/'templates_manifest.json')]:
            p=patch.object(gen,name,value);p.start();self.addCleanup(p.stop)
        self.asset=dict(id='cat_001',categoryDirectory='cats',traceImagePath='templates/cats/cat_001.webp',
                        previewImagePath='templates_preview/cats/cat_001_preview.webp')
        self.trace=self.root/self.asset['traceImagePath'];self.trace.parent.mkdir(parents=True)
        image=Image.new('RGBA',(1024,1024),(0,0,0,0))
        ImageDraw.Draw(image).ellipse((160,160,864,864),outline='black',width=12)
        image.save(self.trace,'WEBP',lossless=True)

    def color_image(self):
        image=Image.new('RGBA',(1024,1024),(0,0,0,0))
        ImageDraw.Draw(image).ellipse((160,160,864,864),fill='#f6ceb8',outline='#342b33',width=12)
        return image

    def encoded(self,image):
        buf=io.BytesIO();image.save(buf,'PNG');return base64.b64encode(buf.getvalue()).decode()

    def test_color_conversion_is_lossless_and_trace_is_untouched(self):
        before=self.trace.read_bytes()
        stats=gen.save_preview(self.encoded(self.color_image()),self.asset)
        self.assertTrue(stats['lossless']);self.assertTrue(stats['transparencyDetected'])
        self.assertGreater(stats['colorPixels'],1000)
        self.assertEqual(before,self.trace.read_bytes())
        self.assertFalse(list(self.root.rglob('*.tmp')))

    def test_empty_opaque_and_monochrome_outputs_rejected(self):
        invalid=[Image.new('RGBA',(1024,1024)),Image.new('RGBA',(1024,1024),'white'),
                 Image.open(self.trace).convert('RGBA')]
        for image in invalid:
            with self.assertRaises(gen.ValidationError):gen.image_stats(image,gen.reference(self.asset))

    def test_different_outline_rejected(self):
        image=Image.new('RGBA',(1024,1024),(0,0,0,0))
        ImageDraw.Draw(image).rectangle((160,160,864,864),fill='#f6ceb8',outline='#342b33',width=12)
        with self.assertRaises(gen.ValidationError):gen.image_stats(image,gen.reference(self.asset))

    def test_preview_cannot_target_trace_directory(self):
        for path in ['templates/cats/cat_001.webp','../cat_001.webp','templates_preview/dogs/cat_001_preview.webp']:
            with self.assertRaises(gen.ValidationError):gen.preview_path(dict(self.asset,previewImagePath=path))

    def test_failed_conversion_preserves_existing_preview(self):
        gen.save_preview(self.encoded(self.color_image()),self.asset)
        path=gen.preview_path(self.asset);before=path.read_bytes()
        with self.assertRaises(Exception):gen.save_preview('not base64',self.asset)
        self.assertEqual(before,path.read_bytes())

    def test_manifest_update_preserves_every_existing_field(self):
        original=[dict(id='cat_001',name='Existing name',displayName='Existing name',category='Cats',
                       difficulty='Easy',featured=True,description='Keep this',shortDescription='Keep this too',
                       imagePath='templates/cats/cat_001.webp',custom='also preserve')]
        gen.MANIFEST.write_text(json.dumps(original))
        gen.update_manifest(original,[self.asset])
        result=json.loads(gen.MANIFEST.read_text())[0]
        self.assertEqual(original[0],{k:v for k,v in result.items() if k not in ('previewImagePath','traceImagePath')})
        self.assertEqual(self.asset['previewImagePath'],result['previewImagePath'])
        self.assertEqual(self.asset['traceImagePath'],result['traceImagePath'])

    def test_concurrent_manifest_change_not_overwritten(self):
        gen.MANIFEST.write_text('[]')
        with self.assertRaises(gen.ValidationError):gen.update_manifest([dict(id='cat_001')],[self.asset])
        self.assertEqual('[]',gen.MANIFEST.read_text())

    def test_expansion_refuses_existing_ids_without_api_calls(self):
        gen.MANIFEST.write_text('[{"id":"cat_001"}]')
        plan=self.root/'plan.json';plan.write_text('[{"id":"cat_001"}]')
        with patch.object(gen,'ROOT',self.root), patch('openai.OpenAI') as client:
            with self.assertRaisesRegex(gen.ValidationError,'unique new IDs'):
                gen.expand_library(plan)
            client.assert_not_called()

    def test_expansion_reserves_budget_before_request(self):
        gen.MANIFEST.write_text('[{"id":"cat_001"}]')
        plan=self.root/'plan.json'
        asset=dict(id='cat_002',categoryDirectory='cats',traceImagePath='templates/cats/cat_002.webp',
                   previewImagePath='templates_preview/cats/cat_002_preview.webp',description='A kitten')
        plan.write_text(json.dumps([asset]))
        before=self.trace.read_bytes()
        with patch.object(gen,'ROOT',self.root), patch.dict('os.environ',{'OPENAI_API_KEY':'test-placeholder'}), patch('openai.OpenAI') as client:
            with self.assertRaisesRegex(gen.ValidationError,'Budget reserve reached'):
                gen.expand_library(plan,budget=0.4)
            client.return_value.images.generate.assert_not_called()
            client.return_value.images.edit.assert_not_called()
        self.assertEqual(before,self.trace.read_bytes())
        self.assertEqual([],json.loads((self.root/'scripts/animal_expansion_report.json').read_text())['requests'])

    def test_legacy_trace_generator_cannot_drop_expansion_records(self):
        import generate_trace_assets as traces
        from types import SimpleNamespace
        gen.MANIFEST.write_text('[{"id":"cat_001"},{"id":"turtle_001"}]')
        before=gen.MANIFEST.read_bytes()
        with patch.object(traces,'ANDROID_MANIFEST',gen.MANIFEST):
            with self.assertRaisesRegex(gen.ValidationError,'Expanded library detected'):
                traces.run(SimpleNamespace(validate_only=False),[{'id':'cat_001'}])
        self.assertEqual(before,gen.MANIFEST.read_bytes())


if __name__=='__main__':unittest.main()
