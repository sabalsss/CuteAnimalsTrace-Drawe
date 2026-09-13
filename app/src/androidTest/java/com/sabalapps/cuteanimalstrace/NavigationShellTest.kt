package com.sabalapps.cuteanimalstrace

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
        compose.onNodeWithTag("drawing_kitten").performClick()
        compose.onNodeWithTag("screen_detail").assertIsDisplayed()
        compose.onNodeWithTag("bottom_navigation").assertDoesNotExist()
        compose.onNodeWithText("Curious kitten").assertIsDisplayed()
        compose.onNodeWithTag("start_trace").performScrollTo().performClick()
        compose.onNodeWithTag("screen_trace").assertIsDisplayed()
        compose.onNodeWithTag("bottom_navigation").assertDoesNotExist()
        compose.onNodeWithText("Selected drawing: Curious kitten").assertIsDisplayed()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("screen_trace").assertIsDisplayed()
        compose.onNodeWithText("Selected drawing: Curious kitten").assertIsDisplayed()
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
}
