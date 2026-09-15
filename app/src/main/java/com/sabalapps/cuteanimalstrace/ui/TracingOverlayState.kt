package com.sabalapps.cuteanimalstrace.ui

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver

/** Positions are fractions of the preview size, so recreation/resizing keeps relative placement. */
@Stable
internal class TracingOverlayState(
    x: Float = 0f, y: Float = 0f, scale: Float = 1f,
    opacity: Float = 0.65f, flipped: Boolean = false, locked: Boolean = false,
) {
    var x by mutableFloatStateOf(x.finiteOr(0f).coerceIn(-MAX_OFFSET, MAX_OFFSET))
        private set
    var y by mutableFloatStateOf(y.finiteOr(0f).coerceIn(-MAX_OFFSET, MAX_OFFSET))
        private set
    var scale by mutableFloatStateOf(scale.finiteOr(1f).coerceIn(MIN_SCALE, MAX_SCALE))
        private set
    var opacity by mutableFloatStateOf(opacity.finiteOr(0.65f).coerceIn(0f, 1f))
        private set
    var flipped by mutableStateOf(flipped)
        private set
    var locked by mutableStateOf(locked)
        private set

    fun updateOpacity(value: Float) { if (value.isFinite()) opacity = value.coerceIn(0f, 1f) }
    fun toggleLock() { locked = !locked }
    fun flip() { if (!locked) flipped = !flipped }
    fun resetPosition() {
        if (!locked) { x = 0f; y = 0f; scale = 1f }
    }

    /** Centroid and pan are in preview-relative coordinates, independent of image flip/scale. */
    fun transform(panX: Float, panY: Float, centroidX: Float, centroidY: Float, zoom: Float) {
        if (locked || !panX.isFinite() || !panY.isFinite() || !centroidX.isFinite() ||
            !centroidY.isFinite() || !zoom.isFinite() || zoom <= 0f) return
        val nextScale = (scale * zoom).coerceIn(MIN_SCALE, MAX_SCALE)
        val ratio = nextScale / scale
        // Keep the point under the pinch centroid stationary while zooming, then apply drag.
        x = (centroidX + (x - centroidX) * ratio + panX).coerceIn(-MAX_OFFSET, MAX_OFFSET)
        y = (centroidY + (y - centroidY) * ratio + panY).coerceIn(-MAX_OFFSET, MAX_OFFSET)
        scale = nextScale
    }

    companion object {
        const val MIN_SCALE = 0.4f
        const val MAX_SCALE = 4f
        // Keep the image center inside a 10% inset, so it can always be dragged back.
        private const val MAX_OFFSET = 0.4f
        private fun Float.finiteOr(fallback: Float) = if (isFinite()) this else fallback

        val Saver = listSaver<TracingOverlayState, Any>(
            save = { listOf(it.x, it.y, it.scale, it.opacity, it.flipped, it.locked) },
            restore = { TracingOverlayState(it[0] as Float, it[1] as Float, it[2] as Float,
                it[3] as Float, it[4] as Boolean, it[5] as Boolean) },
        )
    }
}
