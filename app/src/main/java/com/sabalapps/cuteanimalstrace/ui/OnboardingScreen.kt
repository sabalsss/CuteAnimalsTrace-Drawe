package com.sabalapps.cuteanimalstrace.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.sabalapps.cuteanimalstrace.R
import com.sabalapps.cuteanimalstrace.data.DrawingTemplate
import com.sabalapps.cuteanimalstrace.data.LocalTemplateCatalog
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/** Featured drawings make the friendliest examples; any drawings will do if none are featured. */
internal fun LocalTemplateCatalog.tutorialDrawings() = featuredTemplates.ifEmpty { templates }.take(3)

private class TutorialPage(val title: Int, val body: Int, val illustration: @Composable (List<DrawingTemplate>) -> Unit)

private val TutorialPages = listOf(
    TutorialPage(R.string.tutorial_pick_title, R.string.tutorial_pick_body) { PickIllustration(it) },
    TutorialPage(R.string.tutorial_setup_title, R.string.tutorial_setup_body) { SetupIllustration(it.firstOrNull()) },
    TutorialPage(R.string.tutorial_align_title, R.string.tutorial_align_body) { AlignIllustration(it.firstOrNull()) },
    TutorialPage(R.string.tutorial_trace_title, R.string.tutorial_trace_body) { TraceIllustration(it.firstOrNull()) },
)

/**
 * First-run walkthrough, also reachable from Settings. Each page loops a short illustration built
 * from bundled drawings, so it works offline and never needs extra artwork.
 */
@Composable
fun OnboardingScreen(drawings: List<DrawingTemplate>, onFinish: () -> Unit, handleBack: Boolean = false) {
    val pager = rememberPagerState { TutorialPages.size }
    val scope = rememberCoroutineScope()
    val last = pager.currentPage == TutorialPages.lastIndex
    if (handleBack) BackHandler(onBack = onFinish)
    val compact = LocalConfiguration.current.screenHeightDp < 600

    Surface(Modifier.fillMaxSize().testTag("onboarding"), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().systemBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onFinish, modifier = Modifier.heightIn(min = 48.dp)
                    .testTag("onboarding_skip"), enabled = !last) {
                    Text(stringResource(R.string.tutorial_skip))
                }
            }
            HorizontalPager(pager, Modifier.weight(1f).fillMaxWidth()) { index ->
                val page = TutorialPages[index]
                Column(
                    Modifier.fillMaxSize().padding(horizontal = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(Modifier.size(if (compact) 220.dp else 290.dp), contentAlignment = Alignment.Center) {
                        page.illustration(drawings)
                    }
                    Spacer(Modifier.height(if (compact) 20.dp else 36.dp))
                    Text(stringResource(page.title), style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center, modifier = Modifier.semantics { heading() })
                    Spacer(Modifier.height(12.dp))
                    Text(stringResource(page.body), style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center,
                        modifier = Modifier.widthIn(max = 420.dp))
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(TutorialPages.size) { i ->
                        val selected = i == pager.currentPage
                        val width by animateDpAsState(if (selected) 26.dp else 8.dp, label = "dotWidth")
                        val color by animateColorAsState(
                            if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant, label = "dotColor")
                        Box(Modifier.height(8.dp).width(width).background(color, CircleShape))
                    }
                }
                Button(
                    onClick = { if (last) onFinish() else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } },
                    shape = MaterialTheme.shapes.large,
                    contentPadding = PaddingValues(horizontal = 28.dp, vertical = 14.dp),
                    modifier = Modifier.heightIn(min = 52.dp).testTag("onboarding_next"),
                ) {
                    Text(stringResource(if (last) R.string.tutorial_start else R.string.tutorial_next))
                }
            }
        }
    }
}

/** Soft rounded backdrop shared by every illustration. */
@Composable
private fun Plate(color: Color, content: @Composable BoxScope.() -> Unit) {
    Box(Modifier.fillMaxSize().background(color, RoundedCornerShape(40.dp)),
        contentAlignment = Alignment.Center, content = content)
}

@Composable
private fun loop(millis: Int, label: String): State<Float> =
    rememberInfiniteTransition(label = label).animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(millis, easing = LinearEasing)),
        label = label,
    )

private fun wave(t: Float, phase: Float = 0f) = sin((t + phase) * 2f * PI.toFloat())

/** Three drawings fanned out like cards; the middle one gets a little heart. */
@Composable
private fun PickIllustration(drawings: List<DrawingTemplate>) {
    val t by loop(2600, "pick")
    Plate(MaterialTheme.colorScheme.primaryContainer) {
        val picks = drawings.take(3)
        picks.forEachIndexed { i, drawing ->
            val side = i - 1
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 6.dp,
                modifier = Modifier.size(width = 112.dp, height = 136.dp)
                    .graphicsLayer {
                        translationX = side * 72.dp.toPx()
                        translationY = (if (side == 0) -14f else 8f).dp.toPx() + wave(t, i / 3f) * 5.dp.toPx()
                        rotationZ = side * 12f
                        val pop = if (side == 0) 1f + 0.04f * wave(t) else 0.92f
                        scaleX = pop; scaleY = pop
                    }
                    .zIndex(if (side == 0) 1f else 0f),
            ) {
                AssetTemplateImage(drawing.previewImagePath, null,
                    Modifier.fillMaxSize().padding(10.dp), targetSize = 256)
            }
        }
        if (picks.size >= 2) {
            val beat = ((t * 2f) % 1f).let { if (it < 0.2f) 1f + it * 1.5f else 1.3f - (it - 0.2f) * 0.375f }
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surface, shadowElevation = 4.dp,
                modifier = Modifier.size(40.dp).zIndex(2f).graphicsLayer {
                    translationX = 44.dp.toPx(); translationY = (-76).dp.toPx()
                    scaleX = beat; scaleY = beat
                }) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Favorite, null, Modifier.size(22.dp), tint = Color(0xFFE0605E))
                }
            }
        }
    }
}

/** A phone hovering over paper; its shadow grows as it lowers, hinting at the distance. */
@Composable
private fun SetupIllustration(drawing: DrawingTemplate?) {
    val t by loop(3000, "setup")
    val lift = (wave(t) + 1f) / 2f
    Plate(MaterialTheme.colorScheme.secondaryContainer) {
        Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 30.dp)
            .size(width = 220.dp, height = 96.dp)
            .background(Color.White, RoundedCornerShape(14.dp))
            .border(1.dp, Color(0x1F000000), RoundedCornerShape(14.dp)))
        Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 64.dp)
            .size(width = 96.dp, height = 18.dp)
            .graphicsLayer { scaleX = 1.2f - lift * 0.4f; scaleY = 1.2f - lift * 0.4f; alpha = 0.18f - lift * 0.08f }
            .background(Color.Black, CircleShape))
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = Color(0xFF2B2622),
            shadowElevation = 10.dp,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 18.dp)
                .size(width = 100.dp, height = 168.dp)
                .graphicsLayer { translationY = lift * -14.dp.toPx() },
        ) {
            Box(Modifier.padding(6.dp).background(Color(0xFFF3EDE6), RoundedCornerShape(21.dp)),
                contentAlignment = Alignment.Center) {
                if (drawing != null) {
                    AssetTemplateImage(drawing.traceImagePath, null, Modifier.size(72.dp),
                        targetSize = 256, colorFilter = ColorFilter.tint(Color.Black.copy(alpha = 0.7f)))
                }
                Box(Modifier.align(Alignment.TopCenter).padding(top = 8.dp).size(10.dp)
                    .background(Color(0xFF2B2622), CircleShape))
            }
        }
    }
}

/** The outline breathes bigger and smaller between two finger dots while opacity rises and falls. */
@Composable
private fun AlignIllustration(drawing: DrawingTemplate?) {
    val t by loop(2800, "align")
    val pinch = (wave(t) + 1f) / 2f
    val opacity = 0.35f + 0.65f * ((wave(t, 0.25f) + 1f) / 2f)
    Plate(MaterialTheme.colorScheme.tertiaryContainer) {
        if (drawing != null) {
            AssetTemplateImage(drawing.traceImagePath, null,
                Modifier.size(150.dp).graphicsLayer {
                    val s = 0.8f + 0.25f * pinch
                    scaleX = s; scaleY = s; alpha = opacity
                    translationY = -16.dp.toPx()
                },
                targetSize = 512, colorFilter = ColorFilter.tint(Color.Black))
        }
        listOf(-1f, 1f).forEach { dir ->
            Box(Modifier.size(30.dp).graphicsLayer {
                val spread = (58f + 34f * pinch).dp.toPx()
                translationX = dir * spread * 0.8f
                translationY = dir * spread * 0.55f - 16.dp.toPx()
            }.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), CircleShape)
                .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape))
        }
        Row(Modifier.align(Alignment.BottomCenter).padding(bottom = 26.dp).width(190.dp)
            .background(MaterialTheme.colorScheme.surface, CircleShape).padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(AppIcons.opacity, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Box(Modifier.weight(1f).height(6.dp).background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape)) {
                Box(Modifier.fillMaxHeight().fillMaxWidth(opacity).background(MaterialTheme.colorScheme.primary, CircleShape))
            }
        }
    }
}

/** A pencil draws the outline onto paper, then the lock clicks shut. */
@Composable
private fun TraceIllustration(drawing: DrawingTemplate?) {
    val t by loop(3600, "trace")
    val draw = (t / 0.7f).coerceIn(0f, 1f)
    val locked = t > 0.74f
    val lockScale by animateFloatAsState(if (locked) 1f else 0.6f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "lock")
    Plate(MaterialTheme.colorScheme.primaryContainer) {
        val side: Dp = 190.dp
        Box(Modifier.size(side + 30.dp).background(Color.White, RoundedCornerShape(18.dp))
            .border(1.dp, Color(0x1F000000), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center) {
            Box(Modifier.size(side)) {
                if (drawing != null) {
                    AssetTemplateImage(drawing.traceImagePath, null,
                        Modifier.fillMaxSize().drawWithContent {
                            clipRect(bottom = size.height * draw) { this@drawWithContent.drawContent() }
                        },
                        targetSize = 512, colorFilter = ColorFilter.tint(Color(0xFF3A3531)))
                }
                if (draw < 1f) {
                    Icon(Icons.Default.Edit, null,
                        Modifier.size(40.dp).graphicsLayer {
                            val tipX = 40.dp.toPx() * 3f / 24f
                            val tipY = 40.dp.toPx() * 21f / 24f
                            translationX = side.toPx() * (0.5f + 0.4f * sin(draw * PI.toFloat() * 9f)) - tipX
                            translationY = side.toPx() * draw - tipY
                        },
                        tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
        Surface(shape = CircleShape,
            color = if (locked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
            contentColor = if (locked) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            shadowElevation = 6.dp,
            modifier = Modifier.align(Alignment.TopEnd).padding(14.dp).size(52.dp)
                .graphicsLayer { scaleX = lockScale; scaleY = lockScale }) {
            Box(contentAlignment = Alignment.Center) {
                Icon(if (locked) AppIcons.lockClosed else AppIcons.lockOpen, null, Modifier.size(24.dp))
            }
        }
    }
}
