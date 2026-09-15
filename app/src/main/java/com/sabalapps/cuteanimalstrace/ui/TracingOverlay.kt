package com.sabalapps.cuteanimalstrace.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.sabalapps.cuteanimalstrace.R
import com.sabalapps.cuteanimalstrace.data.DrawingTemplate
import kotlin.math.roundToInt

@Composable
internal fun TracingOverlay(drawing: DrawingTemplate, state: TracingOverlayState) {
    val status = stringResource(if (state.locked) R.string.overlay_locked else R.string.overlay_gestures)
    BoxWithConstraints(
        Modifier.fillMaxSize().clipToBounds().testTag("tracing_overlay")
            .semantics { stateDescription = status }
            .pointerInput(state, state.locked) {
                if (!state.locked) {
                    detectTransformGestures { centroid, pan, zoom, _ ->
                        if (size.width > 0 && size.height > 0) {
                            state.transform(pan.x / size.width, pan.y / size.height,
                                centroid.x / size.width - 0.5f, centroid.y / size.height - 0.5f, zoom)
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        val previewWidth = constraints.maxWidth.toFloat()
        val previewHeight = constraints.maxHeight.toFloat()
        Image(
            painter = painterResource(drawing.imageRes),
            contentDescription = stringResource(R.string.animal_overlay, drawing.name),
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(Color.Black),
            modifier = Modifier.size(minOf(maxWidth, maxHeight) * 0.72f)
                .testTag("overlay_image_${drawing.id}")
                .graphicsLayer {
                    translationX = state.x * previewWidth
                    translationY = state.y * previewHeight
                    scaleX = if (state.flipped) -state.scale else state.scale
                    scaleY = state.scale
                    alpha = state.opacity
                },
        )
    }
}

@Composable
internal fun OverlayControls(state: TracingOverlayState) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth().testTag("overlay_controls"),
    ) {
        BoxWithConstraints(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
            val controlsWidth = maxWidth
            if (controlsWidth >= 600.dp) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OpacityControl(state, Modifier.weight(1f))
                    OverlayActions(state, Modifier.widthIn(max = controlsWidth * 0.55f))
                }
            } else {
                Column {
                    OpacityControl(state, Modifier.fillMaxWidth())
                    OverlayActions(state, Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun OpacityControl(state: TracingOverlayState, modifier: Modifier) {
    val opacityLabel = stringResource(R.string.overlay_opacity)
    Row(modifier, verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(opacityLabel, style = MaterialTheme.typography.labelLarge)
        Slider(value = state.opacity, onValueChange = state::updateOpacity,
            modifier = Modifier.weight(1f).testTag("overlay_opacity")
                .semantics { contentDescription = opacityLabel })
        Text(stringResource(R.string.overlay_opacity_percent, (state.opacity * 100).roundToInt()),
            modifier = Modifier.width(48.dp), style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun OverlayActions(state: TracingOverlayState, modifier: Modifier) {
    Row(modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically) {
        // Lock stays first and keeps its width when toggled, including on small screens.
        FilterChip(selected = state.locked, onClick = state::toggleLock,
            modifier = Modifier.widthIn(min = 88.dp).testTag("overlay_lock"),
            label = { Text(stringResource(if (state.locked) R.string.overlay_unlock else R.string.overlay_lock)) })
        FilterChip(selected = state.flipped, onClick = state::flip, enabled = !state.locked,
            modifier = Modifier.testTag("overlay_flip"),
            label = { Text(stringResource(R.string.overlay_flip)) })
        OutlinedButton(onClick = state::resetPosition, enabled = !state.locked,
            modifier = Modifier.testTag("overlay_reset")) {
            Text(stringResource(R.string.overlay_reset))
        }
    }
}
