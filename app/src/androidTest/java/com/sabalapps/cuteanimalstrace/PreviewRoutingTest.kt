package com.sabalapps.cuteanimalstrace

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.sabalapps.cuteanimalstrace.data.LocalTemplateCatalog
import com.sabalapps.cuteanimalstrace.ui.*
import com.sabalapps.cuteanimalstrace.ui.theme.CuteAnimalsTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PreviewRoutingTest {
    @get:Rule val compose = createComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val original get() = LocalTemplateCatalog.load(context.assets).templates.first()
    // A UI screen that accidentally loads the trace now fails visibly.
    private val previewOnly get() = original.copy(traceImagePath = "missing-trace.webp")

    private fun assertColorImage(grid: String? = null, card: String? = null) {
        if (grid != null && card != null) {
            compose.onNodeWithTag(grid).performScrollToNode(hasTestTag(card))
        }
        val matcher = hasTestTag("template_image_cat_001")
        val loaded = SemanticsMatcher.expectValue(SemanticsProperties.StateDescription,
            context.getString(R.string.artwork_loaded))
        compose.waitUntil(10_000) {
            compose.onAllNodes(matcher and loaded, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
        }
        val pixels = compose.onNode(matcher, useUnmergedTree = true).captureToImage().toPixelMap()
        var colored = 0
        for (y in 0 until pixels.height) for (x in 0 until pixels.width) {
            val c = pixels[x, y]
            if (maxOf(c.red, c.green, c.blue) - minOf(c.red, c.green, c.blue) > 0.10f) colored++
        }
        assertTrue("Preview must contain colorful fills", colored > 100)
    }

    @Test fun homeUsesColorPreview() {
        val drawing = previewOnly
        compose.setContent { CuteAnimalsTheme {
            HomeScreen(listOf(drawing), emptySet(), emptyList(), {}, {}, onDrawing = {})
        } }
        assertColorImage("screen_home", "drawing_cat_001")
    }
    @Test fun recentUsesColorPreview() {
        val drawing = previewOnly
        compose.setContent { CuteAnimalsTheme {
            HomeScreen(emptyList(), emptySet(), listOf(drawing), {}, {}, onDrawing = {})
        } }
        assertColorImage("screen_home", "recent_cat_001")
    }
    @Test fun exploreUsesColorPreview() {
        val drawing = previewOnly
        compose.setContent { CuteAnimalsTheme { ExploreScreen(listOf(drawing), onDrawing = {}) } }
        assertColorImage("screen_explore", "drawing_cat_001")
    }
    @Test fun favoritesUsesColorPreview() {
        val drawing = previewOnly
        compose.setContent { CuteAnimalsTheme { FavoritesScreen(listOf(drawing), {}, {}, {}) } }
        assertColorImage("screen_favorites", "drawing_cat_001")
    }
    @Test fun detailUsesColorPreview() {
        val drawing = previewOnly
        compose.setContent { CuteAnimalsTheme { DrawingDetailScreen(drawing, false, {}, onTrace = {}) } }
        assertColorImage()
    }
    @Test fun tracingLoadsOriginalWithoutDependingOnPreview() {
        val drawing = original.copy(previewImagePath = "missing-preview.webp")
        compose.setContent { CuteAnimalsTheme {
            Box(Modifier.fillMaxSize().background(Color.White)) {
                TracingOverlay(drawing, TracingOverlayState())
            }
        } }
        val loaded = SemanticsMatcher.expectValue(SemanticsProperties.StateDescription,
            context.getString(R.string.artwork_loaded))
        compose.waitUntil(10_000) {
            compose.onAllNodes(loaded, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
        }
        val pixels = compose.onNodeWithTag("tracing_overlay").captureToImage().toPixelMap()
        var ink = 0
        var colored = 0
        for (y in 0 until pixels.height) for (x in 0 until pixels.width) {
            val c = pixels[x, y]
            if (c.red < 0.9f) ink++
            if (maxOf(c.red, c.green, c.blue) - minOf(c.red, c.green, c.blue) > 0.05f) colored++
        }
        assertTrue("Original ink must be visible", ink > 100)
        assertTrue("Tracing must remain monochrome", colored == 0)
    }
}
