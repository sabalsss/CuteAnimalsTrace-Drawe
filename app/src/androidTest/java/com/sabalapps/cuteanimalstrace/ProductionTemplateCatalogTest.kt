package com.sabalapps.cuteanimalstrace

import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.test.platform.app.InstrumentationRegistry
import com.sabalapps.cuteanimalstrace.data.*
import com.sabalapps.cuteanimalstrace.ui.TemplateBitmapLoader
import com.sabalapps.cuteanimalstrace.ui.filterDrawings
import java.io.IOException
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.junit.Assert.*
import org.junit.Test

class ProductionTemplateCatalogTest {
    private val assets get() = InstrumentationRegistry.getInstrumentation().targetContext.assets
    private fun catalog() = LocalTemplateCatalog.load(assets)
    private val expectedCounts = mapOf(
        TemplateCategory.Cats to 15, TemplateCategory.Dogs to 15, TemplateCategory.Bunnies to 10,
        TemplateCategory.Pandas to 8, TemplateCategory.Foxes to 8, TemplateCategory.Bears to 8,
        TemplateCategory.Kawaii to 34, TemplateCategory.BabyAnimals to 10,
    )

    @Test fun manifestHas108UniqueCompleteTemplates_andExactCategoryCounts() {
        val catalog = catalog()
        assertEquals(108, catalog.templates.size)
        assertEquals(108, catalog.templates.map { it.id }.distinct().size)
        assertEquals(expectedCounts, catalog.templates.groupingBy { it.category }.eachCount())
        assertEquals("Kawaii Animals", TemplateCategory.Kawaii.label)
        assertEquals("Baby Animals", TemplateCategory.BabyAnimals.label)
        catalog.templates.forEach {
            assertTrue(it.name.isNotBlank())
            assertTrue(it.description.isNotBlank())
            assertEquals(it, catalog.findById(it.id))
        }
        assertNull(catalog.findById(null))
        assertNull(catalog.findById("missing"))
        assertEquals(16, catalog.featuredTemplates.size)
        assertEquals(catalog.templates.filter { it.featured }, catalog.featuredTemplates)
    }

    @Test fun everyManifestAssetExistsDecodesAndPreservesTransparency() {
        val paths = catalog().templates.map { it.traceImagePath }.toSet()
        val bundled = assets.list("templates")!!.flatMap { category ->
            assets.list("templates/$category")!!.filter { it.endsWith(".webp") }
                .map { "templates/$category/$it" }
        }.toSet()
        assertEquals(paths, bundled)
        catalog().templates.forEach {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            assets.open(it.traceImagePath).use { stream -> BitmapFactory.decodeStream(stream, null, bounds) }
            assertEquals(it.id, 1024, bounds.outWidth)
            assertEquals(it.id, 1024, bounds.outHeight)
            val bitmap = assets.open(it.traceImagePath).use { stream ->
                BitmapFactory.decodeStream(stream, null, BitmapFactory.Options().apply { inSampleSize = 4 })
            }!!
            try {
                assertTrue(it.id, bitmap.hasAlpha())
                val pixels = IntArray(bitmap.width * bitmap.height)
                bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
                assertTrue("Transparent background: " + it.id, pixels.any { color -> Color.alpha(color) == 0 })
                assertTrue("Visible ink: " + it.id, pixels.any { color -> Color.alpha(color) > 128 })
            } finally { bitmap.recycle() }
        }
    }

    @Test fun all108PreviewsAreSeparateColorImagesWithTransparentBackgrounds() {
        val templates = catalog().templates
        val bundled = assets.list("templates_preview")!!.flatMap { category ->
            assets.list("templates_preview/$category")!!.filter { it.endsWith(".webp") }
                .map { "templates_preview/$category/$it" }
        }.toSet()
        assertEquals(108, bundled.size)
        assertEquals(templates.map { it.previewImagePath }.toSet(), bundled)
        templates.forEach { template ->
            assertNotEquals(template.traceImagePath, template.previewImagePath)
            val bitmap = assets.open(template.previewImagePath).use { BitmapFactory.decodeStream(it) }!!
            try {
                assertEquals(1024, bitmap.width)
                assertEquals(1024, bitmap.height)
                val pixels = IntArray(bitmap.width * bitmap.height)
                bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
                assertTrue(template.id, pixels.any { Color.alpha(it) == 0 })
                assertTrue(template.id, pixels.count {
                    Color.alpha(it) > 128 &&
                        maxOf(Color.red(it), Color.green(it), Color.blue(it)) -
                        minOf(Color.red(it), Color.green(it), Color.blue(it)) > 20
                } > 1000)
            } finally { bitmap.recycle() }
        }
    }

    @Test fun imageRolesRejectSwappedAndMismatchedPaths_andSupportLegacyManifest() {
        val original = assets.open("templates_manifest.json").bufferedReader().use { it.readText() }
        val invalid = listOf(
            "traceImagePath" to "templates_preview/cats/cat_001_preview.webp",
            "previewImagePath" to "templates/cats/cat_001.webp",
            "previewImagePath" to "templates_preview/dogs/cat_001_preview.webp",
            "previewImagePath" to "templates_preview/cats/cat_002_preview.webp",
        )
        invalid.forEach { (key, path) ->
            val text = JSONArray(original).apply { getJSONObject(0).put(key, path) }.toString()
            assertThrows(IOException::class.java) { LocalTemplateCatalog.parse(text.reader()) }
        }
        val legacy = JSONArray(original).apply {
            for (index in 0 until length()) {
                getJSONObject(index).remove("traceImagePath")
                getJSONObject(index).remove("previewImagePath")
            }
        }
        LocalTemplateCatalog.parse(legacy.toString().reader()).templates.forEach {
            assertEquals(it.traceImagePath, it.previewImagePath)
        }
    }

    @Test fun productionSearchCategoryDifficultyAndIntersectionsWork() {
        val templates = catalog().templates
        assertEquals(templates, filterDrawings(templates, " ", null, null))
        assertEquals(listOf("cat_001"), filterDrawings(templates, " HAPPY KITTEN ", null, null).map { it.id })
        expectedCounts.forEach { (category, count) ->
            assertEquals(count, filterDrawings(templates, "", category, null).size)
            assertEquals(count, filterDrawings(templates, category.label.uppercase(), null, null).size)
        }
        val difficulties = mapOf(Difficulty.Easy to 50, Difficulty.Medium to 50, Difficulty.Detailed to 8)
        difficulties.forEach { (difficulty, count) ->
            assertEquals(count, filterDrawings(templates, "", null, difficulty).size)
        }
        assertEquals(listOf("cat_015"),
            filterDrawings(templates, "cat", TemplateCategory.Cats, Difficulty.Detailed).map { it.id })
        assertTrue(filterDrawings(templates, "panda", TemplateCategory.Cats, Difficulty.Easy).isEmpty())
        listOf("turtle", "axolotl", "platypus", "kangaroo", "zebra", "octopus", "stingray").forEach { animal ->
            assertEquals(listOf("${animal}_001"),
                filterDrawings(templates, animal, TemplateCategory.Kawaii, null).map { it.id })
        }
    }

    @Test fun malformedAndDuplicateRecordsAreRejected() {
        val original = assets.open("templates_manifest.json").bufferedReader().use { it.readText() }
        val duplicate = JSONArray(original).let { it.put(it.getJSONObject(0)).toString() }
        val invalidCategory = JSONArray(original).apply { getJSONObject(0).put("category", "Unknown") }.toString()
        val unsafePath = JSONArray(original).apply { getJSONObject(0).put("imagePath", "../cat.webp") }.toString()
        val missingName = JSONArray(original).apply { getJSONObject(0).remove("displayName") }.toString()
        listOf(duplicate, invalidCategory, unsafePath, missingName, "[]", "[").forEach { text ->
            assertThrows(IOException::class.java) { LocalTemplateCatalog.parse(text.reader()) }
        }
    }

    @Test fun decoderSamplesThumbnailsAndKeepsOverlayFullResolution() = runBlocking {
        val path = catalog().templates.first().traceImagePath
        val thumbnail = TemplateBitmapLoader.load(assets, path, 512)
        val overlay = TemplateBitmapLoader.load(assets, path, 1024)
        assertEquals(512, thumbnail.width)
        assertEquals(1024, overlay.width)
        assertTrue(overlay.hasAlpha())
        assertSame(thumbnail, TemplateBitmapLoader.load(assets, path, 512))
    }
}
