package com.sabalapps.cuteanimalstrace

import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.performScrollToNode
import com.sabalapps.cuteanimalstrace.data.LocalTemplateCatalog
import androidx.compose.ui.test.*
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.espresso.Espresso.pressBack
import org.junit.Rule
import org.junit.Test

class NavigationShellTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun detailAndTrace_hideTabs_andBackReturnsToExplore() {
        compose.onNodeWithTag("tab_explore").performClick()
        compose.onNodeWithTag("screen_explore").performScrollToNode(hasTestTag("drawing_cat_001"))
        compose.onNodeWithTag("drawing_cat_001").performClick()
        compose.onNodeWithTag("screen_detail").assertIsDisplayed()
        compose.onNodeWithTag("bottom_navigation").assertDoesNotExist()
        compose.onNodeWithText("Happy Kitten").assertIsDisplayed()
        compose.onNodeWithTag("start_trace").performScrollTo().performClick()
        compose.onNodeWithTag("screen_trace").assertIsDisplayed()
        compose.onNodeWithTag("bottom_navigation").assertDoesNotExist()
        compose.onNodeWithText("Selected drawing: Happy Kitten").assertIsDisplayed()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("screen_trace").assertIsDisplayed()
        compose.onNodeWithText("Selected drawing: Happy Kitten").assertIsDisplayed()
        compose.onNodeWithContentDescription("Back").performClick()
        compose.onNodeWithTag("screen_detail").assertIsDisplayed()
        pressBack()
        compose.onNodeWithTag("tab_explore").assertIsSelected()
    }

    @Test
    fun tabsAndHomeCallToAction_useSingleTopNavigation() {
        compose.onNodeWithText("Explore drawings").performClick()
        compose.onNodeWithTag("tab_explore").assertIsSelected()
        repeat(3) { compose.onNodeWithTag("tab_explore").performClick() }
        compose.onNodeWithTag("tab_favorites").performClick()
        compose.onNodeWithTag("screen_favorites").assertIsDisplayed()
        compose.onNodeWithTag("tab_settings").performClick()
        compose.onNodeWithTag("screen_settings").assertIsDisplayed()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("tab_settings").assertIsSelected()
        pressBack()
        compose.onNodeWithTag("tab_home").assertIsSelected()
        compose.onNodeWithTag("tab_explore").performClick()
        compose.onNodeWithTag("screen_explore").assertIsDisplayed()
    }

    @Test
    fun eachCategory_hasArtworkAndOpensMatchingDetails() {
        compose.onNodeWithTag("tab_explore").performClick()
        LocalTemplateCatalog.load(compose.activity.assets).templates.distinctBy { it.category }.forEach { template ->
            compose.onNodeWithTag("screen_explore")
                .performScrollToNode(hasTestTag("drawing_${template.id}"))
            compose.onNodeWithTag("drawing_${template.id}").performClick()
            val loaded = hasTestTag("template_image_${template.id}") and
                SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Artwork loaded")
            compose.waitUntil(10_000) { compose.onAllNodes(loaded).fetchSemanticsNodes().isNotEmpty() }
            compose.onNode(loaded).assertIsDisplayed()
            compose.onNodeWithText(template.name).assertIsDisplayed()
            compose.onNodeWithText("${template.category.label} · ${template.difficulty.label}")
                .performScrollTo().assertIsDisplayed()
            compose.onNodeWithText(template.description).performScrollTo().assertIsDisplayed()
            compose.onNodeWithText("Trace this drawing").performScrollTo().assertIsDisplayed()
            compose.onNodeWithContentDescription("Back").performClick()
        }
    }

    @Test
    fun home_showsFeaturedCatalogAndOpensItsDetails() {
        val template = LocalTemplateCatalog.load(compose.activity.assets).featuredTemplates.last()
        compose.onNodeWithTag("screen_home").performScrollToNode(hasTestTag("drawing_${template.id}"))
        compose.onNodeWithTag("drawing_${template.id}").performClick()
        compose.onNodeWithText(template.name).assertIsDisplayed()
        compose.onNodeWithTag("template_image_${template.id}").assertIsDisplayed()
    }
}
