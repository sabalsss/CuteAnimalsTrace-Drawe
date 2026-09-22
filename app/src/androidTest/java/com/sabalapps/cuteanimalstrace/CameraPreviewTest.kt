package com.sabalapps.cuteanimalstrace

import android.Manifest
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.Lifecycle
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.filters.SdkSuppress
import org.junit.Rule
import org.junit.Test

@SdkSuppress(minSdkVersion = 28)
class CameraPreviewTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    private fun waitForLivePreview() {
        val live = hasTestTag("camera_preview") and
            SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Live camera preview")
        compose.waitUntil(20_000) { compose.onAllNodes(live).fetchSemanticsNodes().isNotEmpty() }
        compose.onNode(live).assertIsDisplayed()
    }

    @Test
    fun previewStreams_restartsAfterBackgroundAndRecreation_andCanReenter() {
        InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(
            compose.activity.packageName, Manifest.permission.CAMERA,
        )
        compose.onNodeWithTag("tab_explore").performClick()
        compose.onNodeWithTag("screen_explore").performScrollToNode(hasTestTag("drawing_cat_001"))
        compose.onNodeWithTag("drawing_cat_001").performClick()
        compose.onNodeWithTag("start_trace").performScrollTo().performClick()
        waitForLivePreview()
        compose.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        compose.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        waitForLivePreview()
        // The production screen supplies a decoded bitmap; its Image exists only after loading.
        val artwork = hasTestTag("overlay_image_cat_001")
        compose.waitUntil(10_000) { compose.onAllNodes(artwork).fetchSemanticsNodes().isNotEmpty() }
        compose.onNode(artwork).assertIsDisplayed()
        compose.onNodeWithTag("overlay_opacity").performSemanticsAction(SemanticsActions.SetProgress) { it(0.4f) }
        compose.onNodeWithTag("overlay_flip").performClick()
        compose.onNodeWithTag("overlay_lock").performClick()
        waitForLivePreview()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("overlay_lock").assertIsSelected()
        compose.onNodeWithTag("overlay_flip").assertIsSelected().assertIsNotEnabled()
        compose.onNodeWithTag("overlay_opacity").assertRangeInfoEquals(ProgressBarRangeInfo(0.4f, 0f..1f))
        waitForLivePreview()
        compose.onNodeWithContentDescription("Back").performClick()
        compose.onNodeWithTag("camera_preview").assertDoesNotExist()
        compose.onNodeWithTag("start_trace").performScrollTo().performClick()
        waitForLivePreview()
    }
}
