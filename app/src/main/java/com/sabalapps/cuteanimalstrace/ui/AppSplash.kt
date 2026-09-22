package com.sabalapps.cuteanimalstrace.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.sabalapps.cuteanimalstrace.R
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/** Minimum time on screen, so the artwork is seen instead of flashing past on fast devices. */
private const val SplashFillMillis = 1300
private const val SplashFinishMillis = 280

/**
 * Picks up where the system splash leaves off: the full app artwork with a progress bar that
 * fills while the catalog and preferences load. The bar waits just short of full until [ready],
 * then completes and calls [onFinished].
 */
@Composable
fun AppSplash(ready: Boolean, onFinished: () -> Unit) {
    val progress = remember { Animatable(0f) }
    val entrance = remember { Animatable(0.82f) }
    val finish by rememberUpdatedState(onFinished)
    LaunchedEffect(Unit) {
        launch {
            entrance.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow))
        }
        progress.animateTo(0.88f, tween(SplashFillMillis, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(ready, progress.isRunning) {
        if (ready && !progress.isRunning && progress.value >= 0.88f) {
            progress.animateTo(1f, tween(SplashFinishMillis))
            finish()
        }
    }
    val float by rememberInfiniteTransition(label = "splashFloat").animateFloat(
        initialValue = -4f, targetValue = 4f,
        animationSpec = infiniteRepeatable(tween(1600), RepeatMode.Reverse),
        label = "float",
    )
    val loading = stringResource(R.string.splash_loading)
    val percent = (progress.value * 100).roundToInt()

    Surface(Modifier.fillMaxSize().testTag("app_splash"), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier.fillMaxSize().systemBarsPadding().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painterResource(R.drawable.app_icon_art), null,
                Modifier.size(176.dp)
                    .graphicsLayer {
                        scaleX = entrance.value; scaleY = entrance.value
                        translationY = float.dp.toPx()
                    }
                    .shadow(12.dp, RoundedCornerShape(44.dp))
                    .clip(RoundedCornerShape(44.dp)),
            )
            Spacer(Modifier.height(28.dp))
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(28.dp))
            LinearProgressIndicator(
                progress = { progress.value },
                modifier = Modifier.width(200.dp).height(8.dp)
                    .semantics { contentDescription = loading }
                    .testTag("splash_progress"),
                strokeCap = StrokeCap.Round,
                gapSize = 0.dp,
                drawStopIndicator = {},
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            )
            Spacer(Modifier.height(10.dp))
            Text(stringResource(R.string.splash_progress, percent),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
