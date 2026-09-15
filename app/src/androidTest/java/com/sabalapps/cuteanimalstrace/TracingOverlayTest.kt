package com.sabalapps.cuteanimalstrace

import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.sabalapps.cuteanimalstrace.data.LocalTemplateCatalog
import com.sabalapps.cuteanimalstrace.ui.*
import com.sabalapps.cuteanimalstrace.ui.theme.CuteAnimalsTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class TracingOverlayTest {
    @get:Rule val compose = createComposeRule()
    private val state = TracingOverlayState()
    private fun showOverlay() {
        compose.setContent {
            CuteAnimalsTheme {
                Column(Modifier.fillMaxSize()) {
                    Box(Modifier.weight(1f)) { TracingOverlay(LocalTemplateCatalog.templates.first(), state) }
                    OverlayControls(state)
                }
            }
        }
    }
    private fun pinch() {
        compose.onNodeWithTag("tracing_overlay").performTouchInput {
            down(0, center - Offset(60f, 0f))
            down(1, center + Offset(60f, 0f))
            moveTo(0, center - Offset(140f, 0f))
            moveTo(1, center + Offset(140f, 0f))
            up(0)
            up(1)
        }
    }
    @Test fun dragPinchFlipAndResetUpdateOverlay() {
        showOverlay()
        compose.onNodeWithTag("tracing_overlay").performTouchInput {
            swipe(center, center + Offset(100f, 70f))
        }
        compose.runOnIdle { assertTrue(state.x > 0f); assertTrue(state.y > 0f) }
        pinch()
        compose.runOnIdle { assertTrue(state.scale > 1f) }
        compose.onNodeWithTag("overlay_flip").performClick().assertIsSelected()
        compose.onNodeWithTag("overlay_reset").performClick()
        compose.runOnIdle {
            assertEquals(0f, state.x, 0f)
            assertEquals(0f, state.y, 0f)
            assertEquals(1f, state.scale, 0f)
            assertTrue(state.flipped)
        }
    }
    @Test fun addingAndRemovingSecondFingerDoesNotJump_andLockCancelsActiveDrag() {
        showOverlay()
        val overlay = compose.onNodeWithTag("tracing_overlay")
        overlay.performTouchInput {
            down(0, center)
            moveTo(0, center + Offset(70f, 0f))
        }
        var previousX = 0f
        compose.runOnIdle { previousX = state.x; assertTrue(previousX > 0f) }
        overlay.performTouchInput {
            down(1, center + Offset(150f, 0f))
            move()
            up(1)
            move()
        }
        compose.runOnIdle {
            assertEquals(previousX, state.x, 0.001f)
            assertEquals(1f, state.scale, 0.001f)
            state.toggleLock()
        }
        overlay.performTouchInput {
            moveTo(0, center + Offset(120f, 0f))
            up(0)
        }
        compose.runOnIdle { assertEquals(previousX, state.x, 0.001f) }
    }

    @Test fun opacityDragUpdatesWhileFingerIsStillDown() {
        showOverlay()
        compose.onNodeWithTag("overlay_opacity").performTouchInput {
            down(center)
            moveTo(Offset(width * 0.8f, center.y))
        }
        compose.runOnIdle {
            assertTrue(state.opacity > 0.7f)
            assertEquals(0f, state.x, 0f)
            assertEquals(1f, state.scale, 0f)
        }
        compose.onNodeWithTag("overlay_opacity").performTouchInput { up() }
    }

    @Test fun lockedOverlayIgnoresTouchAndKeepsOpacityAvailable() {
        showOverlay()
        compose.onNodeWithTag("overlay_lock").performClick().assertIsSelected()
        compose.onNodeWithTag("overlay_reset").assertIsNotEnabled()
        compose.onNodeWithTag("overlay_flip").assertIsNotEnabled()
        compose.onNodeWithTag("tracing_overlay").performTouchInput {
            swipe(center, center + Offset(120f, 80f))
        }
        pinch()
        compose.onNodeWithTag("overlay_opacity").performSemanticsAction(SemanticsActions.SetProgress) { it(0.3f) }
        compose.runOnIdle {
            assertEquals(0f, state.x, 0f)
            assertEquals(0f, state.y, 0f)
            assertEquals(1f, state.scale, 0f)
            assertEquals(0.3f, state.opacity, 0.001f)
        }
        compose.onNodeWithTag("overlay_lock").performClick()
        compose.onNodeWithTag("tracing_overlay").performTouchInput {
            swipe(center, center + Offset(100f, 0f))
        }
        compose.runOnIdle { assertTrue(state.x > 0f) }
    }
}
