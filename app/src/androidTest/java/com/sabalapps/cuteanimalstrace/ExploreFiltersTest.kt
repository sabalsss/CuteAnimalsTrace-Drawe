package com.sabalapps.cuteanimalstrace

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test

class ExploreFiltersTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    private fun openExplore() = compose.onNodeWithTag("tab_explore").performClick()
    private fun show(tag: String) {
        compose.onNodeWithTag("screen_explore").performScrollToNode(hasTestTag(tag))
    }

    @Test
    fun searchMatchesCategoryAndName_caseInsensitively() {
        openExplore()
        compose.onNodeWithTag("explore_search").performTextInput("  CATS  ")
        compose.onNodeWithTag("explore_search").performImeAction()
        show("result_count")
        compose.onNodeWithTag("result_count").assertTextEquals("15 drawings")
        show("drawing_cat_015")
        compose.onNodeWithTag("drawing_cat_015").assertIsDisplayed()
        show("explore_search")
        compose.onNodeWithTag("explore_search").performTextReplacement("CURIOUS KITTEN")
        compose.onNodeWithTag("explore_search").performImeAction()
        show("result_count")
        compose.onNodeWithTag("result_count").assertTextEquals("1 drawing")
        show("drawing_cat_007")
        compose.onNodeWithTag("drawing_cat_007").assertIsDisplayed()
    }

    @Test
    fun combinedFilters_emptyStateCanResetEverything() {
        openExplore()
        compose.onNodeWithTag("explore_search").performTextInput("panda")
        compose.onNodeWithTag("explore_search").performImeAction()
        compose.onNodeWithTag("category_Cats").performScrollTo().performClick()
        compose.onNodeWithTag("difficulty_Medium").performScrollTo().performClick()
        show("clear_filters")
        compose.onNodeWithTag("explore_empty").assertIsDisplayed()
        compose.onNodeWithTag("clear_filters").performClick()
        show("result_count")
        compose.onNodeWithTag("result_count").assertTextEquals("108 drawings")
        show("explore_search")
        compose.onNodeWithTag("explore_search").assertTextContains("")
        compose.onNodeWithTag("category_all").assertIsSelected()
        compose.onNodeWithTag("difficulty_all").assertIsSelected()
    }

    @Test
    fun selectionsSurviveRecreationTabsAndDetailNavigation() {
        openExplore()
        compose.onNodeWithTag("explore_search").performTextInput("cat")
        compose.onNodeWithTag("explore_search").performImeAction()
        compose.onNodeWithTag("category_Cats").performScrollTo().performClick()
        compose.onNodeWithTag("difficulty_Detailed").performScrollTo().performClick()
        compose.activityRule.scenario.recreate()
        show("result_count")
        compose.onNodeWithTag("result_count").assertTextEquals("1 drawing")
        compose.onNodeWithTag("tab_home").performClick()
        compose.onNodeWithTag("tab_explore").performClick()
        show("drawing_cat_015")
        compose.onNodeWithTag("drawing_cat_015").performClick()
        compose.onNodeWithText("Cozy Cat").assertIsDisplayed()
        compose.onNodeWithContentDescription("Back").performClick()
        show("explore_search")
        compose.onNodeWithTag("explore_search").assertTextContains("cat")
        compose.onNodeWithTag("category_Cats").assertIsSelected()
        compose.onNodeWithTag("difficulty_Detailed").assertIsSelected()
    }
}
