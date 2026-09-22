package com.sabalapps.cuteanimalstrace.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sabalapps.cuteanimalstrace.R
import kotlin.math.sin

/** Artwork is authored in this square unit space, then scaled to whatever room it is given. */
private const val CanvasUnit = 120f

/**
 * An original, hand-drawn-feeling sad kitten for the empty Favorites shelf.
 * Everything is vector geometry, so it stays crisp on tablets and costs nothing to keep looping:
 * a slow float, a lazy tail sway, an ear twitch, an occasional blink and one slow tear.
 */
@Composable
internal fun SadCat(modifier: Modifier = Modifier, size: Dp = 156.dp) {
    val transition = rememberInfiniteTransition(label = "sadCat")
    val float by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3200, easing = LinearEasing)),
        label = "float",
    )
    val tail by transition.animateFloat(
        initialValue = -6f, targetValue = 8f,
        animationSpec = infiniteRepeatable(tween(2400), RepeatMode.Reverse),
        label = "tail",
    )
    val ear by transition.animateFloat(
        initialValue = -2f, targetValue = 5f,
        animationSpec = infiniteRepeatable(tween(1900), RepeatMode.Reverse),
        label = "ear",
    )
    // One cycle holds the eyes open for most of its length, then closes them briefly.
    val blinkPhase by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4200, easing = LinearEasing)),
        label = "blink",
    )
    val tearPhase by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(5200, easing = LinearEasing)),
        label = "tear",
    )

    val fur = MaterialTheme.colorScheme.secondaryContainer
    val ink = MaterialTheme.colorScheme.onSecondaryContainer
    val innerEar = MaterialTheme.colorScheme.secondary.copy(alpha = 0.32f)
    val tearColor = MaterialTheme.colorScheme.primary
    val description = stringResource(R.string.favorites_empty_illustration)

    Box(
        modifier.size(size).testTag("favorites_empty_cat")
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(size)) {
            val factor = this.size.minDimension / CanvasUnit
            val lift = sin(float * 2f * Math.PI.toFloat()) * 3.5f
            scale(factor, pivot = Offset.Zero) {
                translate(top = lift) {
                    drawCat(fur, ink, innerEar, tearColor, tail, ear, blinkPhase, tearPhase)
                }
            }
        }
    }
}

private fun DrawScope.drawCat(
    fur: Color,
    ink: Color,
    innerEar: Color,
    tearColor: Color,
    tailAngle: Float,
    earAngle: Float,
    blinkPhase: Float,
    tearPhase: Float,
) {
    val outline = Stroke(width = 2.2f, cap = StrokeCap.Round)

    // Tail: swings from where it meets the body, never from its tip.
    rotate(tailAngle, pivot = Offset(74f, 92f)) {
        val tail = Path().apply {
            moveTo(74f, 92f)
            cubicTo(98f, 94f, 104f, 74f, 92f, 64f)
        }
        drawPath(tail, fur, style = Stroke(width = 9f, cap = StrokeCap.Round))
        drawPath(tail, ink.copy(alpha = 0.35f), style = Stroke(width = 1.4f, cap = StrokeCap.Round))
    }

    // Body.
    drawOval(fur, topLeft = Offset(26f, 66f), size = Size(68f, 46f))
    drawOval(ink.copy(alpha = 0.35f), topLeft = Offset(26f, 66f), size = Size(68f, 46f), style = outline)

    // Front paws.
    drawOval(fur, topLeft = Offset(38f, 96f), size = Size(18f, 12f))
    drawOval(fur, topLeft = Offset(62f, 96f), size = Size(18f, 12f))
    drawOval(ink.copy(alpha = 0.3f), topLeft = Offset(38f, 96f), size = Size(18f, 12f),
        style = Stroke(width = 1.6f))
    drawOval(ink.copy(alpha = 0.3f), topLeft = Offset(62f, 96f), size = Size(18f, 12f),
        style = Stroke(width = 1.6f))

    // Ears; only the right one twitches, which reads as alive rather than restless.
    drawEar(Offset(32f, 44f), Offset(26f, 18f), Offset(50f, 30f), fur, innerEar, ink, 0f, false)
    drawEar(Offset(88f, 44f), Offset(94f, 18f), Offset(70f, 30f), fur, innerEar, ink, earAngle, true)

    // Head.
    drawCircle(fur, radius = 30f, center = Offset(60f, 48f))
    drawCircle(ink.copy(alpha = 0.35f), radius = 30f, center = Offset(60f, 48f), style = outline)

    // Eyes: a soft squash for the blink, closed eyes become a gentle downward curve.
    val open = blinkOpenness(blinkPhase)
    drawEye(Offset(48f, 46f), open, ink)
    drawEye(Offset(72f, 46f), open, ink)

    // Sad brows: high at the nose, drooping away towards the ears.
    drawLine(ink.copy(alpha = 0.7f), Offset(41f, 39f), Offset(52f, 34f), strokeWidth = 2f,
        cap = StrokeCap.Round)
    drawLine(ink.copy(alpha = 0.7f), Offset(79f, 39f), Offset(68f, 34f), strokeWidth = 2f,
        cap = StrokeCap.Round)

    // Nose and a small frown.
    val nose = Path().apply {
        moveTo(56f, 56f); lineTo(64f, 56f); lineTo(60f, 60f); close()
    }
    drawPath(nose, ink.copy(alpha = 0.75f))
    val frown = Path().apply {
        moveTo(52f, 68f)
        quadraticTo(60f, 61f, 68f, 68f)
    }
    drawPath(frown, ink.copy(alpha = 0.7f), style = Stroke(width = 2f, cap = StrokeCap.Round))

    // Whiskers.
    listOf(-4f, 3f).forEach { drop ->
        drawLine(ink.copy(alpha = 0.45f), Offset(38f, 56f + drop), Offset(24f, 53f + drop * 1.6f),
            strokeWidth = 1.4f, cap = StrokeCap.Round)
        drawLine(ink.copy(alpha = 0.45f), Offset(82f, 56f + drop), Offset(96f, 53f + drop * 1.6f),
            strokeWidth = 1.4f, cap = StrokeCap.Round)
    }

    drawTear(tearPhase, tearColor)
}

/** Open for most of the loop, with a quick close-and-open near the end. */
private fun blinkOpenness(phase: Float): Float {
    val blinkStart = 0.88f
    if (phase < blinkStart) return 1f
    val local = (phase - blinkStart) / (1f - blinkStart)
    return kotlin.math.abs(local * 2f - 1f).coerceIn(0.05f, 1f)
}

private fun DrawScope.drawEye(center: Offset, open: Float, ink: Color) {
    if (open > 0.15f) {
        drawOval(
            ink,
            topLeft = Offset(center.x - 4.4f, center.y - 5.4f * open),
            size = Size(8.8f, 10.8f * open),
        )
        drawCircle(Color.White.copy(alpha = 0.85f * open), radius = 1.5f,
            center = Offset(center.x + 1.4f, center.y - 2f * open))
    } else {
        val lid = Path().apply {
            moveTo(center.x - 4.6f, center.y)
            quadraticTo(center.x, center.y + 3.2f, center.x + 4.6f, center.y)
        }
        drawPath(lid, ink, style = Stroke(width = 2f, cap = StrokeCap.Round))
    }
}

private fun DrawScope.drawEar(
    base: Offset, tip: Offset, inner: Offset,
    fur: Color, innerEar: Color, ink: Color,
    angle: Float, mirrored: Boolean,
) {
    rotate(if (mirrored) angle else -angle, pivot = base) {
        val ear = Path().apply {
            moveTo(base.x, base.y); lineTo(tip.x, tip.y); lineTo(inner.x, inner.y); close()
        }
        drawPath(ear, fur)
        drawPath(ear, ink.copy(alpha = 0.35f), style = Stroke(width = 2f, cap = StrokeCap.Round))
        // Same triangle pulled halfway towards its own centre, so it always sits inside the ear.
        val centre = Offset((base.x + tip.x + inner.x) / 3f, (base.y + tip.y + inner.y) / 3f)
        fun inset(point: Offset) = Offset(
            centre.x + (point.x - centre.x) * 0.55f,
            centre.y + (point.y - centre.y) * 0.55f,
        )
        val small = Path().apply {
            val a = inset(base); val b = inset(tip); val c = inset(inner)
            moveTo(a.x, a.y); lineTo(b.x, b.y); lineTo(c.x, c.y); close()
        }
        drawPath(small, innerEar)
    }
}

/** A single slow tear: it swells, slides down the cheek and fades before it ever lands. */
private fun DrawScope.drawTear(phase: Float, color: Color) {
    if (phase > 0.55f) return
    val local = phase / 0.55f
    val alpha = when {
        local < 0.2f -> local / 0.2f
        local > 0.75f -> (1f - local) / 0.25f
        else -> 1f
    }.coerceIn(0f, 1f)
    val y = 52f + local * 22f
    drawCircle(color.copy(alpha = alpha * 0.85f), radius = 2.6f, center = Offset(44f, y))
    drawPath(
        Path().apply {
            moveTo(44f, y - 4.6f)
            lineTo(46.4f, y - 0.4f)
            lineTo(41.6f, y - 0.4f)
            close()
        },
        color.copy(alpha = alpha * 0.85f),
    )
}

/**
 * Favorites' empty shelf. The kitten shrinks on short screens and at large font scales so the
 * heading, message and call to action always stay reachable; on tablets the column simply centres.
 */
@Composable
internal fun FavoritesEmptyState(onExplore: () -> Unit, modifier: Modifier = Modifier) {
    val configuration = LocalConfiguration.current
    val catSize = when {
        configuration.screenHeightDp < 520 || configuration.fontScale > 1.5f -> 108.dp
        configuration.screenWidthDp >= 600 -> 188.dp
        else -> 156.dp
    }
    Column(
        modifier.fillMaxWidth().padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SadCat(size = catSize)
        Text(
            stringResource(R.string.favorites_empty_title),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            stringResource(R.string.favorites_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 320.dp),
        )
        Button(
            onClick = onExplore,
            modifier = Modifier.padding(top = 4.dp).heightIn(min = 50.dp)
                .testTag("favorites_empty_explore"),
            shape = MaterialTheme.shapes.large,
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
        ) { Text(stringResource(R.string.explore_drawings)) }
    }
}
