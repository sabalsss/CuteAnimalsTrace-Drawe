package com.sabalapps.cuteanimalstrace.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sabalapps.cuteanimalstrace.R
import com.sabalapps.cuteanimalstrace.data.Difficulty
import com.sabalapps.cuteanimalstrace.data.DrawingTemplate
import com.sabalapps.cuteanimalstrace.data.TemplateCategory

/** Hand-drawn style glyphs the bundled Material icon set does not ship. */
internal object AppIcons {
    val paw: Painter @Composable get() = painterResource(R.drawable.ic_paw)
    val sparkle: Painter @Composable get() = painterResource(R.drawable.ic_sparkle)
    val photo: Painter @Composable get() = painterResource(R.drawable.ic_photo)
    val flip: Painter @Composable get() = painterResource(R.drawable.ic_flip)
    val lockClosed: Painter @Composable get() = painterResource(R.drawable.ic_lock_closed)
    val lockOpen: Painter @Composable get() = painterResource(R.drawable.ic_lock_open)
    val flashlight: Painter @Composable get() = painterResource(R.drawable.ic_flashlight)
    val opacity: Painter @Composable get() = painterResource(R.drawable.ic_opacity)
    val grid: Painter @Composable get() = painterResource(R.drawable.ic_grid)
    val palette: Painter @Composable get() = painterResource(R.drawable.ic_palette)
}

/** Artwork sits on a soft pastel plate keyed to its category, in both themes. */
internal data class ArtworkTint(val top: Color, val bottom: Color, val accent: Color)

@Composable
internal fun artworkTint(category: TemplateCategory): ArtworkTint {
    val dark = MaterialTheme.colorScheme.surface.luminanceIsDark()
    return remember(category, dark) {
        if (dark) when (category) {
            TemplateCategory.Cats -> ArtworkTint(Color(0xFF3A2D25), Color(0xFF2A211C), Color(0xFFE7B58C))
            TemplateCategory.Dogs -> ArtworkTint(Color(0xFF3A3424), Color(0xFF29251A), Color(0xFFE6CC8A))
            TemplateCategory.Bunnies -> ArtworkTint(Color(0xFF3A272C), Color(0xFF2A1D20), Color(0xFFECAEB9))
            TemplateCategory.Pandas -> ArtworkTint(Color(0xFF262D34), Color(0xFF1B2126), Color(0xFFA9C2D6))
            TemplateCategory.Foxes -> ArtworkTint(Color(0xFF3B2A20), Color(0xFF2A1E18), Color(0xFFEFAE7D))
            TemplateCategory.Bears -> ArtworkTint(Color(0xFF342B22), Color(0xFF261F19), Color(0xFFD9B98F))
            TemplateCategory.Kawaii -> ArtworkTint(Color(0xFF2E2739), Color(0xFF211C29), Color(0xFFC3AEEA))
            TemplateCategory.BabyAnimals -> ArtworkTint(Color(0xFF22322B), Color(0xFF19241F), Color(0xFF97D5B4))
        } else when (category) {
            TemplateCategory.Cats -> ArtworkTint(Color(0xFFFFEBDC), Color(0xFFFFF8F2), Color(0xFFB4713E))
            TemplateCategory.Dogs -> ArtworkTint(Color(0xFFFFF2D4), Color(0xFFFFFBF0), Color(0xFF9A7A22))
            TemplateCategory.Bunnies -> ArtworkTint(Color(0xFFFFE6EB), Color(0xFFFFF7F8), Color(0xFFB65C6E))
            TemplateCategory.Pandas -> ArtworkTint(Color(0xFFE8EFF6), Color(0xFFF8FAFC), Color(0xFF4F6B85))
            TemplateCategory.Foxes -> ArtworkTint(Color(0xFFFFE4CE), Color(0xFFFFF7EF), Color(0xFFB4652C))
            TemplateCategory.Bears -> ArtworkTint(Color(0xFFF5E7D4), Color(0xFFFDF8F1), Color(0xFF8A6A42))
            TemplateCategory.Kawaii -> ArtworkTint(Color(0xFFEFE7FB), Color(0xFFFAF7FE), Color(0xFF6D53A8))
            TemplateCategory.BabyAnimals -> ArtworkTint(Color(0xFFDDF2E5), Color(0xFFF5FCF8), Color(0xFF2F7355))
        }
    }
}

private fun Color.luminanceIsDark() = (red * 0.299f + green * 0.587f + blue * 0.114f) < 0.5f

/** Gentle squeeze while a card is held, so taps feel responsive without bouncing the grid. */
@Composable
internal fun Modifier.pressScale(source: MutableInteractionSource, pressedScale: Float = 0.965f): Modifier {
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMediumLow),
        label = "pressScale",
    )
    return graphicsLayer { scaleX = scale; scaleY = scale }
}

/** Grid cells widen with the font scale so large-text users never get clipped names. */
@Composable
internal fun adaptiveCellWidth(base: Dp = 168.dp): Dp =
    base * LocalDensity.current.fontScale.coerceIn(1f, 1.6f)

/** Calm mint track instead of the default coral, which read as an error state. */
@Composable
internal fun cuteSliderColors() = SliderDefaults.colors(
    thumbColor = MaterialTheme.colorScheme.primary,
    activeTrackColor = MaterialTheme.colorScheme.primary,
    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
    activeTickColor = Color.Transparent,
    inactiveTickColor = Color.Transparent,
)

@Composable
internal fun isWideScreen(): Boolean = LocalConfiguration.current.screenWidthDp >= 600

@Composable
internal fun SectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.semantics { heading() })
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (trailing != null) trailing()
    }
}

/** Three rising bars; filled bars communicate the level without relying on colour alone. */
@Composable
internal fun DifficultyBars(difficulty: Difficulty, tint: Color, modifier: Modifier = Modifier) {
    val level = when (difficulty) {
        Difficulty.Easy -> 1
        Difficulty.Medium -> 2
        Difficulty.Detailed -> 3
    }
    Row(modifier, verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(3) { index ->
            Box(
                Modifier.width(3.dp).height((5 + index * 3).dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (index < level) tint else tint.copy(alpha = 0.22f)),
            )
        }
    }
}

@Composable
internal fun DifficultyBadge(difficulty: Difficulty, tint: ArtworkTint, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
    ) {
        Row(
            Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            DifficultyBars(difficulty, tint.accent)
            Text(difficulty.label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

/** The soft plate every colour preview sits on; artwork is always the focal point. */
@Composable
internal fun ArtworkPlate(
    drawing: DrawingTemplate,
    modifier: Modifier = Modifier,
    targetSize: Int = 512,
    contentDescription: String? = null,
    artworkPadding: Dp = 12.dp,
    shape: androidx.compose.ui.graphics.Shape = MaterialTheme.shapes.large,
    tagged: Boolean = true,
    overlay: @Composable BoxScope.() -> Unit = {},
) {
    val tint = artworkTint(drawing.category)
    Box(
        modifier.clip(shape)
            .background(Brush.verticalGradient(listOf(tint.top, tint.bottom))),
    ) {
        AssetTemplateImage(
            imagePath = drawing.previewImagePath,
            targetSize = targetSize,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize().padding(artworkPadding)
                .then(if (tagged) Modifier.testTag("template_image_${drawing.id}") else Modifier),
        )
        overlay()
    }
}

/** Heart settles with a small spring so saving feels physical. */
@Composable
internal fun FavoriteButton(
    drawing: DrawingTemplate,
    favorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onPlate: Boolean = false,
    plateColor: Color? = null,
) {
    val favoriteStatus = stringResource(if (favorite) R.string.saved_favorite else R.string.not_saved_favorite)
    val scale by animateFloatAsState(
        targetValue = if (favorite) 1.14f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "favoriteScale",
    )
    val tint by animateColorAsState(
        targetValue = when {
            favorite -> MaterialTheme.colorScheme.secondary
            onPlate -> MaterialTheme.colorScheme.onSurfaceVariant
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = "favoriteTint",
    )
    val content: @Composable () -> Unit = {
        IconToggleButton(
            checked = favorite,
            onCheckedChange = { onClick() },
            modifier = Modifier.fillMaxSize()
                .testTag("favorite_${drawing.id}")
                .semantics { stateDescription = favoriteStatus },
        ) {
            Icon(
                imageVector = if (favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = stringResource(
                    if (favorite) R.string.remove_favorite else R.string.add_favorite, drawing.name),
                tint = tint,
                modifier = Modifier.size(22.dp).graphicsLayer { scaleX = scale; scaleY = scale },
            )
        }
    }
    val box = modifier.sizeIn(minWidth = 44.dp, minHeight = 44.dp)
    if (onPlate) {
        val resting = plateColor ?: MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
        val container by animateColorAsState(
            targetValue = if (favorite) MaterialTheme.colorScheme.secondaryContainer else resting,
            label = "favoriteContainer",
        )
        Surface(
            modifier = box,
            shape = CircleShape,
            color = container,
            shadowElevation = 1.dp,
            content = content,
        )
    } else {
        Box(box, contentAlignment = Alignment.Center) { content() }
    }
}

/** Artwork-first card used by Home, Explore, Favorites and related rows. */
@Composable
internal fun DrawingCard(
    drawing: DrawingTemplate,
    favorite: Boolean,
    onFavorite: (() -> Unit)?,
    modifier: Modifier = Modifier,
    tag: String = "drawing_${drawing.id}",
    showDifficulty: Boolean = true,
    onClick: () -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    val tint = artworkTint(drawing.category)
    Card(
        onClick = onClick,
        interactionSource = source,
        modifier = modifier.fillMaxWidth().pressScale(source).testTag(tag),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
    ) {
        Column {
            ArtworkPlate(
                drawing = drawing,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f).padding(6.dp),
                targetSize = 512,
                artworkPadding = 10.dp,
                shape = MaterialTheme.shapes.medium,
            ) {
                if (showDifficulty) {
                    DifficultyBadge(drawing.difficulty, tint,
                        Modifier.align(Alignment.TopStart).padding(8.dp))
                }
                if (onFavorite != null) {
                    FavoriteButton(drawing, favorite, onFavorite,
                        modifier = Modifier.align(Alignment.TopEnd).padding(6.dp).size(44.dp),
                        onPlate = true)
                }
            }
            Column(
                Modifier.padding(start = 14.dp, end = 14.dp, top = 4.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                Text(drawing.name, style = MaterialTheme.typography.titleMedium,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(drawing.category.label, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

/** Friendly, low-clutter placeholder shared by the empty collections. */
@Composable
internal fun EmptyState(
    title: String,
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    iconVector: ImageVector? = null,
    actionModifier: Modifier = Modifier,
) {
    Column(
        modifier.fillMaxWidth().padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.size(84.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (iconVector != null) {
                    Icon(iconVector, null, Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer)
                } else {
                    Icon(icon ?: AppIcons.paw, null, Modifier.size(38.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
        }
        Text(title, style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center, modifier = Modifier.semantics { heading() })
        Text(message, style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(2.dp))
        Button(onClick = onAction, modifier = actionModifier.heightIn(min = 50.dp),
            shape = MaterialTheme.shapes.large,
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)) {
            Text(actionLabel)
        }
    }
}
