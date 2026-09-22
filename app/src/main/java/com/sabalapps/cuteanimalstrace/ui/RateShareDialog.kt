package com.sabalapps.cuteanimalstrace.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sabalapps.cuteanimalstrace.R
import kotlinx.coroutines.delay

/**
 * The friendly invitation shown at trace milestones. Entrance is a gentle scale/fade with a small
 * settle; the only looping motion is a slow sparkle twinkle, so it never nags for attention.
 */
@Composable
fun RateShareDialog(
    onRate: () -> Unit,
    onShare: () -> Unit,
    onDismiss: () -> Unit,
) {
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }
    val scale by animateFloatAsState(
        targetValue = if (entered) 1f else 0.88f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow),
        label = "promptScale",
    )
    val alpha by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = tween(durationMillis = 180),
        label = "promptAlpha",
    )
    val compact = LocalConfiguration.current.screenHeightDp < 560 ||
        LocalConfiguration.current.fontScale > 1.4f
    var rating by rememberSaveable { mutableIntStateOf(0) }
    val (titleRes, messageRes) = when {
        rating == 0 -> R.string.rate_share_title to R.string.rate_share_message
        rating <= 2 -> R.string.rate_feedback_low_title to R.string.rate_feedback_low_message
        rating == 3 -> R.string.rate_feedback_mid_title to R.string.rate_feedback_mid_message
        else -> R.string.rate_feedback_high_title to R.string.rate_feedback_high_message
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .widthIn(max = 420.dp)
                .testTag("rate_share_prompt")
                .graphicsLayer {
                    scaleX = scale; scaleY = scale; this.alpha = alpha
                },
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            tonalElevation = 3.dp,
            shadowElevation = 6.dp,
        ) {
            Column(
                Modifier.verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (!compact) PromptIllustration(rating)
                Text(
                    stringResource(titleRes),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    stringResource(messageRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                StarRating(rating, onRate = { rating = it })
                Spacer(Modifier.height(2.dp))
                Button(
                    onClick = onRate,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("prompt_rate"),
                    shape = MaterialTheme.shapes.large,
                ) { Text(stringResource(R.string.rate_share_rate)) }
                FilledTonalButton(
                    onClick = onShare,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("prompt_share"),
                    shape = MaterialTheme.shapes.large,
                ) { Text(stringResource(R.string.rate_share_share)) }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("prompt_later"),
                    shape = MaterialTheme.shapes.large,
                ) { Text(stringResource(R.string.rate_share_later)) }
            }
        }
    }
}

/**
 * Five tappable stars. Picking one pops the stars up to it in a quick cascade; the choice only
 * changes the wording, and the Play review button stays available for every rating.
 */
@Composable
private fun StarRating(rating: Int, onRate: (Int) -> Unit) {
    val haptics = LocalHapticFeedback.current
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.selectableGroup().testTag("rate_stars")) {
        (1..5).forEach { star ->
            val selected = star <= rating
            val pop = remember { Animatable(1f) }
            LaunchedEffect(rating) {
                if (selected) {
                    delay((star - 1) * 45L)
                    pop.snapTo(0.7f)
                    pop.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium))
                }
            }
            val tint by animateColorAsState(
                if (selected) StarGold else MaterialTheme.colorScheme.outlineVariant,
                label = "starTint",
            )
            val label = stringResource(R.string.rate_star_description, star)
            Box(
                Modifier.size(48.dp)
                    .selectable(selected = star == rating, role = Role.RadioButton,
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onRate(star)
                        })
                    .semantics { contentDescription = label }
                    .testTag("rate_star_$star"),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Star, null,
                    Modifier.size(36.dp).graphicsLayer { scaleX = pop.value; scaleY = pop.value },
                    tint = tint)
            }
        }
    }
}

private val StarGold = Color(0xFFF4B63F)

/** Paw on a mint plate with two slow sparkles; decorative, so it carries no description. */
@Composable
private fun PromptIllustration(rating: Int) {
    val transition = rememberInfiniteTransition(label = "promptSparkle")
    val twinkle by transition.animateFloat(
        initialValue = 0.35f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800), RepeatMode.Reverse),
        label = "twinkle",
    )
    val drift by transition.animateFloat(
        initialValue = -2.5f, targetValue = 2.5f,
        animationSpec = infiniteRepeatable(tween(2600), RepeatMode.Reverse),
        label = "drift",
    )
    // Each new rating gives the paw a little hop; happier ratings hop higher.
    val hop = remember { Animatable(0f) }
    LaunchedEffect(rating) {
        if (rating > 0) {
            hop.animateTo(-4f * rating, tween(140))
            hop.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
    }
    val plate by animateColorAsState(
        if (rating >= 4) MaterialTheme.colorScheme.tertiaryContainer
            else MaterialTheme.colorScheme.primaryContainer,
        label = "plate",
    )
    Box(Modifier.size(104.dp), contentAlignment = Alignment.Center) {
        Surface(
            shape = CircleShape,
            color = plate,
            modifier = Modifier.size(84.dp)
                .graphicsLayer { translationY = drift + hop.value },
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(AppIcons.paw, null, Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
        Icon(
            AppIcons.sparkle, null,
            Modifier.align(Alignment.TopEnd).size(20.dp)
                .graphicsLayer { alpha = twinkle; scaleX = twinkle; scaleY = twinkle },
            tint = MaterialTheme.colorScheme.secondary,
        )
        Icon(
            AppIcons.sparkle, null,
            Modifier.align(Alignment.BottomStart).size(14.dp)
                .graphicsLayer {
                    alpha = 1.35f - twinkle; scaleX = 1.35f - twinkle; scaleY = 1.35f - twinkle
                },
            tint = MaterialTheme.colorScheme.tertiary,
        )
    }
}
