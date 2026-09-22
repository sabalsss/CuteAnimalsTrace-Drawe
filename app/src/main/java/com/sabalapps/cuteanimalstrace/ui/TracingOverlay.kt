package com.sabalapps.cuteanimalstrace.ui

import android.graphics.Bitmap
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.sabalapps.cuteanimalstrace.R
import com.sabalapps.cuteanimalstrace.data.DrawingTemplate
import kotlin.math.roundToInt
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import kotlin.math.PI
import kotlin.math.sin

@Composable
internal fun TracingOverlay(drawing: DrawingTemplate?, state: TracingOverlayState, bitmap: Bitmap? = null) {
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
        val side = minOf(maxWidth, maxHeight) * (if (drawing == null) 0.82f else 0.72f)
        // The outer box moves and scales with gestures; opacity and flip stay on the artwork so the
        // pen keeps full strength and never draws mirrored.
        val frameModifier = Modifier.size(side)
            .testTag("overlay_image_${drawing?.id ?: "own"}")
            .graphicsLayer {
                translationX = state.x * previewWidth
                translationY = state.y * previewHeight
                scaleX = state.scale
                scaleY = state.scale
            }
        val reveal = rememberPenReveal(drawing?.id ?: bitmap?.generationId?.toString())
        val imageModifier = Modifier.fillMaxSize()
            .graphicsLayer {
                scaleX = if (state.flipped) -1f else 1f
                alpha = state.opacity
            }
            .drawWithContent {
                if (reveal.value >= 1f) drawContent()
                else clipRect(bottom = size.height * penY(reveal.value)) { this@drawWithContent.drawContent() }
            }
        val description = if (drawing != null) stringResource(R.string.animal_overlay, drawing.name)
            else stringResource(R.string.own_image_overlay)
        Box(frameModifier) {
            if (bitmap != null) {
                Image(bitmap.asImageBitmap(), description, imageModifier, contentScale = ContentScale.Fit,
                    colorFilter = if (drawing != null) ColorFilter.tint(Color.Black) else null)
            } else if (drawing != null) {
                AssetTemplateImage(drawing.traceImagePath, description, imageModifier,
                    targetSize = 1024, colorFilter = ColorFilter.tint(Color.Black))
            }
            if (reveal.value < 1f) DrawingPen(reveal.value, side)
        }
    }
}

private const val PenRevealMillis = 3000
private const val PenStrokes = 4.5f

/** Top-to-bottom reveal edge at a steady pace, so every part of the drawing gets its moment. */
private fun penY(progress: Float) = progress

/** Side-to-side hatching across the artwork, like shading in the lines row by row. */
private fun penX(progress: Float) = 0.5f + 0.4f * sin(progress * PI.toFloat() * 2f * PenStrokes)

/** Plays once per artwork; a saved flag keeps rotation or returning from the background quiet. */
@Composable
private fun rememberPenReveal(key: String?): Animatable<Float, *> {
    var played by rememberSaveable(key) { mutableStateOf(false) }
    val reveal = remember(key) { Animatable(if (played) 1f else 0f) }
    LaunchedEffect(reveal) {
        if (reveal.value < 1f) reveal.animateTo(1f, tween(PenRevealMillis, easing = LinearEasing))
        played = true
    }
    return reveal
}

/** Pencil whose tip rides the reveal edge; decorative, so it is hidden from accessibility. */
@Composable
private fun DrawingPen(progress: Float, side: Dp) {
    val penSize = 44.dp
    val fade = ((1f - progress) / 0.12f).coerceIn(0f, 1f)
    Icon(
        Icons.Default.Edit, null,
        Modifier.size(penSize)
            .offset {
                // The Material pencil's tip sits near (3, 21) on its 24-unit grid.
                val tipX = penSize.toPx() * 3f / 24f
                val tipY = penSize.toPx() * 21f / 24f
                IntOffset((side.toPx() * penX(progress) - tipX).roundToInt(),
                    (side.toPx() * penY(progress) - tipY).roundToInt())
            }
            .graphicsLayer { alpha = fade; rotationZ = -6f * sin(progress * PI.toFloat() * 2f * PenStrokes) }
            .clearAndSetSemantics { },
        tint = MaterialTheme.colorScheme.primary,
    )
}

/** Translucent panel that floats over the camera without covering the traced area. */
@Composable
internal fun OverlayControls(state: TracingOverlayState, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.93f),
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 3.dp,
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth().testTag("overlay_controls"),
    ) {
        BoxWithConstraints(Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
            val controlsWidth = maxWidth
            if (controlsWidth >= 600.dp) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OpacityControl(state, Modifier.weight(1f))
                    OverlayActions(state, Modifier.widthIn(max = controlsWidth * 0.55f))
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
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
        Icon(AppIcons.opacity, null, Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Slider(value = state.opacity, onValueChange = state::updateOpacity,
            colors = cuteSliderColors(),
            modifier = Modifier.weight(1f).testTag("overlay_opacity")
                .semantics { contentDescription = opacityLabel })
        Text(stringResource(R.string.overlay_opacity_percent, (state.opacity * 100).roundToInt()),
            modifier = Modifier.widthIn(min = 44.dp), style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun OverlayActions(state: TracingOverlayState, modifier: Modifier) {
    val lockDescription = stringResource(if (state.locked) R.string.locked_state else R.string.unlocked_state)
    val flipDescription = stringResource(if (state.flipped) R.string.flipped_state else R.string.original_state)
    val lockContainer by animateColorAsState(
        targetValue = if (state.locked) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surfaceContainerHigh,
        label = "lockContainer",
    )
    Row(modifier.horizontalScroll(rememberScrollState()).padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically) {
        // Lock stays first and keeps its width when toggled, including on small screens.
        FilterChip(
            selected = state.locked,
            onClick = state::toggleLock,
            modifier = Modifier.heightIn(min = 46.dp).widthIn(min = 104.dp).testTag("overlay_lock")
                .semantics { stateDescription = lockDescription },
            shape = RoundedCornerShape(50),
            leadingIcon = {
                Icon(if (state.locked) AppIcons.lockClosed else AppIcons.lockOpen, null,
                    Modifier.size(18.dp))
            },
            label = { Text(stringResource(if (state.locked) R.string.overlay_unlock else R.string.overlay_lock)) },
            colors = FilterChipDefaults.filterChipColors(
                containerColor = lockContainer,
                selectedContainerColor = lockContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
            ),
            border = FilterChipDefaults.filterChipBorder(
                enabled = true, selected = state.locked,
                borderColor = MaterialTheme.colorScheme.outlineVariant,
                selectedBorderColor = Color.Transparent,
            ),
        )
        FilterChip(
            selected = state.flipped,
            onClick = state::flip,
            enabled = !state.locked,
            modifier = Modifier.heightIn(min = 46.dp).testTag("overlay_flip")
                .semantics { stateDescription = flipDescription },
            shape = RoundedCornerShape(50),
            leadingIcon = { Icon(AppIcons.flip, null, Modifier.size(18.dp)) },
            label = { Text(stringResource(R.string.overlay_flip)) },
            colors = FilterChipDefaults.filterChipColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                selectedLeadingIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ),
            border = FilterChipDefaults.filterChipBorder(
                enabled = !state.locked, selected = state.flipped,
                borderColor = MaterialTheme.colorScheme.outlineVariant,
                selectedBorderColor = Color.Transparent,
            ),
        )
        TextButton(
            onClick = state::resetPosition,
            enabled = !state.locked,
            shape = RoundedCornerShape(50),
            modifier = Modifier.heightIn(min = 46.dp).testTag("overlay_reset"),
        ) {
            Icon(Icons.Default.Refresh, null, Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(stringResource(R.string.overlay_reset))
        }
    }
}
