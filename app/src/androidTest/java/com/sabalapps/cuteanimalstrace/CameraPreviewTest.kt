package com.sabalapps.cuteanimalstrace

import android.Manifest
import androidx.compose.ui.semantics.SemanticsProperties
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
        compose.onNodeWithTag("screen_explore").performScrollToNode(hasTestTag("drawing_kitten"))
        compose.onNodeWithTag("drawing_kitten").performClick()
        compose.onNodeWithTag("start_trace").performScrollTo().performClick()
        waitForLivePreview()
        compose.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        compose.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        waitForLivePreview()
        compose.activityRule.scenario.recreate()
        waitForLivePreview()
        compose.onNodeWithContentDescription("Back").performClick()
        compose.onNodeWithTag("camera_preview").assertDoesNotExist()
        compose.onNodeWithTag("start_trace").performScrollTo().performClick()
        waitForLivePreview()
    }
}
