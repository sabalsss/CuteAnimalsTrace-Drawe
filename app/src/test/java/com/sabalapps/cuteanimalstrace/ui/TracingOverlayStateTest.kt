package com.sabalapps.cuteanimalstrace.ui

import org.junit.Assert.*
import org.junit.Test

class TracingOverlayStateTest {
    @Test fun pinchKeepsItsCentroidAnchored() {
        val state = TracingOverlayState()
        state.transform(0f, 0f, 0.1f, 0.15f, 2f)
        assertEquals(2f, state.scale, 0.0001f)
        assertEquals(-0.1f, state.x, 0.0001f)
        assertEquals(-0.15f, state.y, 0.0001f)
    }
    @Test fun movementAndScaleStayBounded_andInvalidInputIsIgnored() {
        val state = TracingOverlayState()
        state.transform(9f, -9f, 0f, 0f, 100f)
        assertEquals(0.4f, state.x, 0f)
        assertEquals(-0.4f, state.y, 0f)
        assertEquals(4f, state.scale, 0f)
        state.transform(0f, 0f, 0f, 0f, 0.001f)
        assertEquals(0.4f, state.scale, 0f)
        state.transform(Float.NaN, 0f, 0f, 0f, 2f)
        assertTrue(state.x.isFinite())
        assertEquals(0.4f, state.scale, 0f)
    }
    @Test fun draggingBackFromBoundaryRespondsImmediately() {
        val state = TracingOverlayState()
        state.transform(100f, -100f, 0f, 0f, 1f)
        state.transform(-0.05f, 0.05f, 0f, 0f, 1f)
        assertEquals(0.35f, state.x, 0.0001f)
        assertEquals(-0.35f, state.y, 0.0001f)
    }
    @Test fun restoredGeometryIsSafeAndResetRemainsUseful() {
        val state = TracingOverlayState(x = 9f, y = Float.NaN, scale = 0f)
        assertEquals(0.4f, state.x, 0f)
        assertEquals(0f, state.y, 0f)
        assertEquals(0.4f, state.scale, 0f)
        state.resetPosition()
        assertEquals(0f, state.x, 0f)
        assertEquals(1f, state.scale, 0f)
    }
    @Test fun lockBlocksAllGeometryButAllowsOpacity() {
        val state = TracingOverlayState(x = 0.2f, scale = 2f)
        state.toggleLock()
        state.transform(0.2f, 0.2f, 0f, 0f, 2f)
        state.resetPosition()
        state.flip()
        state.updateOpacity(0.3f)
        assertEquals(0.2f, state.x, 0f)
        assertEquals(2f, state.scale, 0f)
        assertFalse(state.flipped)
        assertEquals(0.3f, state.opacity, 0f)
    }
    @Test fun resetCentersAtDefaultScale_preservingOpacityAndFlip() {
        val state = TracingOverlayState(x = 0.3f, y = -0.2f, scale = 2.5f, opacity = 0.4f, flipped = true)
        state.resetPosition()
        assertEquals(0f, state.x, 0f)
        assertEquals(0f, state.y, 0f)
        assertEquals(1f, state.scale, 0f)
        assertEquals(0.4f, state.opacity, 0f)
        assertTrue(state.flipped)
    }
    @Test fun flipDoesNotReverseDragDirection_andUnlockRestoresMovement() {
        val state = TracingOverlayState()
        state.flip()
        state.toggleLock()
        state.toggleLock()
        state.transform(0.1f, 0f, 0f, 0f, 1f)
        assertTrue(state.flipped)
        assertEquals(0.1f, state.x, 0f)
        state.updateOpacity(-1f)
        assertEquals(0f, state.opacity, 0f)
        state.updateOpacity(2f)
        assertEquals(1f, state.opacity, 0f)
    }
}
