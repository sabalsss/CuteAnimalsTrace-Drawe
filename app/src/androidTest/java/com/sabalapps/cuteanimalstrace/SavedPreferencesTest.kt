package com.sabalapps.cuteanimalstrace

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.datastore.preferences.core.edit
import androidx.test.platform.app.InstrumentationRegistry
import com.sabalapps.cuteanimalstrace.data.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class SavedPreferencesTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val repository get() = UserPreferencesRepository(context.userPreferencesStore)

    @Before fun clearSavedState() { runBlocking { context.userPreferencesStore.edit { it.clear() } } }
    @After fun cleanUp() { runBlocking { context.userPreferencesStore.edit { it.clear() } } }

    private fun awaitHome() {
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("screen_home").fetchSemanticsNodes().isNotEmpty() }
    }
    private fun openKitten() {
        compose.onNodeWithTag("tab_explore").performClick()
        compose.onNodeWithTag("screen_explore").performScrollToNode(hasTestTag("drawing_cat_001"))
        compose.onNodeWithTag("drawing_cat_001").performClick()
    }

    @Test fun favoritesStayInSyncAcrossCardsDetailsAndActivityRestart() {
        awaitHome()
        compose.onNodeWithTag("tab_favorites").performClick()
        compose.onNodeWithTag("favorites_empty").assertIsDisplayed()
        openKitten()
        compose.onNodeWithTag("favorite_cat_001").performClick()
        compose.waitUntil { runBlocking { "cat_001" in repository.data.first().favorites } }
        compose.onNodeWithTag("favorite_cat_001").assertIsOn()
        compose.activityRule.scenario.recreate()
        compose.waitUntil { compose.onAllNodesWithTag("favorite_cat_001").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("favorite_cat_001").assertIsOn()
        compose.onNodeWithContentDescription("Back").performClick()
        compose.onNodeWithTag("favorite_cat_001").assertIsOn()
        compose.onNodeWithTag("tab_favorites").performClick()
        compose.onNodeWithTag("drawing_cat_001").assertIsDisplayed()
        compose.onNodeWithTag("favorite_cat_001").performClick()
        compose.waitUntil { runBlocking { repository.data.first().favorites.isEmpty() } }
        compose.onNodeWithTag("favorites_empty").assertIsDisplayed()
    }

    @Test fun recentlyViewedAppearsOnHome_andClearRecentDoesNotClearFavorites() {
        awaitHome()
        openKitten()
        compose.onNodeWithTag("favorite_cat_001").performClick()
        compose.waitUntil { runBlocking { repository.data.first().recent == listOf("cat_001") } }
        compose.onNodeWithContentDescription("Back").performClick()
        compose.onNodeWithTag("tab_home").performClick()
        compose.onNodeWithTag("screen_home").performScrollToNode(hasTestTag("recent_cat_001"))
        compose.onNodeWithTag("recent_cat_001").assertIsDisplayed()
        compose.onNodeWithTag("tab_settings").performClick()
        compose.onNodeWithTag("clear_recent").performScrollTo().performClick()
        compose.onNode(hasText(context.getString(R.string.clear_recent)) and hasClickAction() and
            hasAnyAncestor(isDialog())).performClick()
        compose.waitUntil { runBlocking { repository.data.first().recent.isEmpty() } }
        compose.onNodeWithTag("clear_recent").assertIsNotEnabled()
        compose.onNodeWithTag("tab_home").performClick()
        compose.onNodeWithTag("recent_heading").assertDoesNotExist()
        compose.onNodeWithTag("tab_favorites").performClick()
        compose.onNodeWithTag("drawing_cat_001").assertIsDisplayed()
    }

    @androidx.test.filters.SdkSuppress(minSdkVersion = 28)
    @Test fun traceReadsDefaultsAndReleasesKeepAwakeOnExit() {
        awaitHome()
        runBlocking {
            repository.setDefaultOpacity(0.4f)
            repository.setShowTips(false)
            repository.setKeepAwake(true)
        }
        InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(
            context.packageName, android.Manifest.permission.CAMERA)
        openKitten()
        compose.onNodeWithTag("start_trace").performScrollTo().performClick()
        compose.onNodeWithTag("tracing_tip").assertDoesNotExist()
        compose.onNodeWithTag("overlay_opacity").assert(
            SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.ProgressBarRangeInfo,
                androidx.compose.ui.semantics.ProgressBarRangeInfo(0.4f, 0f..1f)))
        fun keepsAwake(view: android.view.View): Boolean = view.keepScreenOn ||
            (view is android.view.ViewGroup && (0 until view.childCount).any { keepsAwake(view.getChildAt(it)) })
        compose.runOnIdle { assertTrue(keepsAwake(compose.activity.window.decorView)) }
        compose.onNodeWithContentDescription("Back").performClick()
        compose.runOnIdle { assertFalse(keepsAwake(compose.activity.window.decorView)) }
    }

    @Test fun settingsPersistAndHelpDialogOpens() {
        awaitHome()
        compose.onNodeWithTag("tab_settings").performClick()
        compose.onNodeWithTag("theme_Dark").performClick()
        compose.onNodeWithTag("dynamic_color").performClick()
        compose.onNodeWithTag("keep_awake").performScrollTo().performClick()
        compose.onNodeWithTag("show_tips").performScrollTo().performClick()
        compose.onNodeWithTag("default_opacity").performScrollTo()
            .performSemanticsAction(SemanticsActions.SetProgress) { it(0.4f) }
        compose.waitUntil {
            runBlocking {
                val p = repository.data.first()
                p.appearance == Appearance.Dark && p.dynamicColor && !p.keepAwake && !p.showTips &&
                    kotlin.math.abs(p.defaultOpacity - 0.4f) < 0.01f
            }
        }
        compose.activityRule.scenario.recreate()
        compose.waitUntil { compose.onAllNodesWithTag("screen_settings").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("theme_Dark").performScrollTo().assertIsSelected()
        compose.onNodeWithTag("dynamic_color").assertIsOn()
        compose.onNodeWithTag("keep_awake").performScrollTo().assertIsOff()
        compose.onNodeWithTag("show_tips").performScrollTo().assertIsOff()
        compose.onNodeWithText("How to trace").performScrollTo().performClick()
        compose.onNodeWithText("Close").performClick()
        compose.onNodeWithText(context.getString(R.string.privacy_policy)).performScrollTo().assertIsDisplayed()
        val version = context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()
        compose.onNodeWithText(context.getString(R.string.app_version, version)).performScrollTo().assertIsDisplayed()
    }
}
