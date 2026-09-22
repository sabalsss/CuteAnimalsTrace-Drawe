package com.sabalapps.cuteanimalstrace.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sabalapps.cuteanimalstrace.R
import com.sabalapps.cuteanimalstrace.data.Difficulty
import com.sabalapps.cuteanimalstrace.data.DrawingTemplate
import com.sabalapps.cuteanimalstrace.data.TemplateCategory

private val ColumnMaxWidth = 720.dp
private val GridMaxWidth = 1120.dp

@Composable
private fun ScreenColumn(tag: String, content: @Composable ColumnScope.() -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier.widthIn(max = ColumnMaxWidth).fillMaxWidth().testTag(tag)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            content = content,
        )
    }
}

@Composable
private fun DrawingGrid(tag: String, content: LazyGridScope.() -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(adaptiveCellWidth()),
            modifier = Modifier.widthIn(max = GridMaxWidth).fillMaxSize().testTag(tag),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 32.dp),
            content = content,
        )
    }
}

/** Full-width rows inside the adaptive grid. */
private fun LazyGridScope.fullWidth(key: String, content: @Composable () -> Unit) {
    item(key, span = { GridItemSpan(maxLineSpan) }) { content() }
}

@Composable
private fun PageHeading(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.semantics { heading() })
        Text(subtitle, style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ---------------------------------------------------------------- Home

@Composable
private fun BrandHeader() {
    Row(verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(44.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(AppIcons.paw, null, Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
        Column {
            Text(stringResource(R.string.home_brand_badge),
                style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.home_tagline),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun HeroCard(showcase: DrawingTemplate?, onExplore: () -> Unit) {
    val wide = isWideScreen()
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier.padding(if (wide) 24.dp else 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(Modifier.weight(1f).widthIn(max = 520.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(AppIcons.sparkle, null, Modifier.size(16.dp))
                    Text(stringResource(R.string.hero_title),
                        style = if (wide) MaterialTheme.typography.titleMedium
                            else MaterialTheme.typography.labelLarge)
                }
                Text(stringResource(R.string.hero_description),
                    style = if (wide) MaterialTheme.typography.bodyLarge
                        else MaterialTheme.typography.bodyMedium)
                Button(
                    onClick = onExplore,
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary),
                    contentPadding = PaddingValues(horizontal = 22.dp, vertical = 12.dp),
                    modifier = Modifier.heightIn(min = 50.dp),
                ) {
                    Text(stringResource(R.string.explore_drawings))
                }
            }
            if (showcase != null) {
                ArtworkPlate(
                    drawing = showcase,
                    modifier = Modifier.size(if (wide) 156.dp else 112.dp),
                    targetSize = if (wide) 512 else 256,
                    artworkPadding = 8.dp,
                    shape = MaterialTheme.shapes.large,
                    tagged = false,
                )
            }
        }
    }
}

@Composable
private fun OwnImageCard(onClick: () -> Unit) {
    val source = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        interactionSource = source,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = Modifier.fillMaxWidth().pressScale(source).heightIn(min = 76.dp),
    ) {
        Row(
            Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
                modifier = Modifier.size(46.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(AppIcons.photo, null, Modifier.size(24.dp))
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stringResource(R.string.trace_own_image),
                    style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.own_image_hint),
                    style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Default.Create, null, Modifier.size(20.dp))
        }
    }
}

@Composable
private fun CategoryRow(counts: Map<TemplateCategory, Int>, onCategory: (TemplateCategory) -> Unit) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        TemplateCategory.entries.forEach { category ->
            val count = counts[category] ?: return@forEach
            val tint = artworkTint(category)
            val source = remember(category) { MutableInteractionSource() }
            Surface(
                onClick = { onCategory(category) },
                interactionSource = source,
                shape = MaterialTheme.shapes.medium,
                color = tint.top,
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.width(128.dp).pressScale(source)
                    .testTag("home_category_${category.name}"),
            ) {
                Column(
                    Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Surface(shape = CircleShape, color = tint.accent.copy(alpha = 0.18f),
                        modifier = Modifier.size(34.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(AppIcons.paw, null, Modifier.size(18.dp), tint = tint.accent)
                        }
                    }
                    Text(category.label, style = MaterialTheme.typography.titleSmall,
                        maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(stringResource(R.string.category_count, count),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    drawings: List<DrawingTemplate>,
    favorites: Set<String>,
    recent: List<DrawingTemplate>,
    onFavorite: (String) -> Unit,
    onExplore: () -> Unit,
    onOwnImage: () -> Unit = {},
    easyPicks: List<DrawingTemplate> = emptyList(),
    categoryCounts: Map<TemplateCategory, Int> = emptyMap(),
    onCategory: (TemplateCategory) -> Unit = {},
    onDrawing: (String) -> Unit,
) {
    DrawingGrid("screen_home") {
        fullWidth("brand") {
            Column(Modifier.padding(top = 8.dp, bottom = 4.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)) {
                BrandHeader()
                Text(stringResource(R.string.home_headline),
                    style = MaterialTheme.typography.displaySmall,
                    modifier = Modifier.semantics { heading() })
            }
        }
        fullWidth("hero") { HeroCard(drawings.firstOrNull(), onExplore) }
        fullWidth("own_image") { OwnImageCard(onOwnImage) }
        if (categoryCounts.isNotEmpty()) {
            fullWidth("categories_heading") {
                SectionHeader(stringResource(R.string.browse_categories),
                    modifier = Modifier.padding(top = 6.dp))
            }
            fullWidth("categories") { CategoryRow(categoryCounts, onCategory) }
        }
        if (drawings.isNotEmpty()) {
            fullWidth("featured_heading") {
                SectionHeader(stringResource(R.string.little_inspiration),
                    stringResource(R.string.featured_subtitle),
                    modifier = Modifier.padding(top = 6.dp))
            }
            items(drawings, key = { "featured_" + it.id }) { drawing ->
                DrawingCard(drawing, drawing.id in favorites, { onFavorite(drawing.id) }) {
                    onDrawing(drawing.id)
                }
            }
        }
        if (easyPicks.isNotEmpty()) {
            fullWidth("easy_heading") {
                SectionHeader(stringResource(R.string.easy_to_trace),
                    stringResource(R.string.easy_to_trace_subtitle),
                    modifier = Modifier.padding(top = 6.dp))
            }
            items(easyPicks, key = { "easy_" + it.id }) { drawing ->
                DrawingCard(drawing, drawing.id in favorites, { onFavorite(drawing.id) },
                    tag = "easy_${drawing.id}") { onDrawing(drawing.id) }
            }
        }
        if (recent.isNotEmpty()) {
            fullWidth("recent_heading") {
                SectionHeader(stringResource(R.string.recently_viewed),
                    stringResource(R.string.recently_viewed_subtitle),
                    modifier = Modifier.padding(top = 6.dp).testTag("recent_heading"))
            }
            items(recent, key = { "recent_" + it.id }) { drawing ->
                DrawingCard(drawing, drawing.id in favorites, { onFavorite(drawing.id) },
                    tag = "recent_${drawing.id}") { onDrawing(drawing.id) }
            }
        }
    }
}

// ---------------------------------------------------------------- Explore

@Composable
private fun SearchField(
    query: String,
    onQuery: (String) -> Unit,
    onSearch: () -> Unit,
) {
    TextField(
        value = query,
        onValueChange = onQuery,
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("explore_search"),
        placeholder = { Text(stringResource(R.string.search_placeholder)) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            AnimatedVisibility(query.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
                IconButton(onClick = { onQuery("") }) {
                    Icon(Icons.Default.Close, stringResource(R.string.clear_search))
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(50),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
            disabledIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
    )
}

@Composable
private fun CuteFilterChip(selected: Boolean, label: String, tag: String, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = Modifier.heightIn(min = 44.dp).testTag(tag),
        shape = RoundedCornerShape(50),
        label = { Text(label, style = MaterialTheme.typography.labelLarge) },
        leadingIcon = {
            AnimatedVisibility(selected,
                enter = fadeIn() + expandHorizontally(clip = false),
                exit = fadeOut() + shrinkHorizontally(clip = false)) {
                Icon(Icons.Default.Check, null, Modifier.size(FilterChipDefaults.IconSize))
            }
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.outlineVariant,
            selectedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
        ),
    )
}

@Composable
fun ExploreScreen(
    drawings: List<DrawingTemplate>,
    favorites: Set<String> = emptySet(),
    onFavorite: (String) -> Unit = {},
    categoryRequest: TemplateCategory? = null,
    onCategoryHandled: () -> Unit = {},
    onDrawing: (String) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf<TemplateCategory?>(null) }
    var difficulty by rememberSaveable { mutableStateOf<Difficulty?>(null) }
    val matches = remember(drawings, query, category, difficulty) {
        filterDrawings(drawings, query, category, difficulty)
    }
    val focusManager = LocalFocusManager.current
    val clearFilters = {
        query = ""
        category = null
        difficulty = null
        focusManager.clearFocus()
    }
    // A category tapped on Home pre-selects the matching chip exactly once.
    val handled by rememberUpdatedState(onCategoryHandled)
    LaunchedEffect(categoryRequest) {
        if (categoryRequest != null) {
            query = ""
            difficulty = null
            category = categoryRequest
            handled()
        }
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(adaptiveCellWidth()),
            modifier = Modifier.widthIn(max = GridMaxWidth).fillMaxSize().imePadding()
                .testTag("screen_explore"),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item(key = "filters", span = { GridItemSpan(maxLineSpan) }) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    PageHeading(stringResource(R.string.explore_headline),
                        stringResource(R.string.explore_subtitle),
                        Modifier.padding(top = 8.dp))
                    SearchField(query, { query = it }) { focusManager.clearFocus() }
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CuteFilterChip(category == null, stringResource(R.string.all_categories),
                            "category_all") { category = null }
                        TemplateCategory.entries.forEach { option ->
                            CuteFilterChip(category == option, option.label,
                                "category_${option.name}") { category = option }
                        }
                    }
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CuteFilterChip(difficulty == null, stringResource(R.string.all_difficulties),
                            "difficulty_all") { difficulty = null }
                        Difficulty.entries.forEach { option ->
                            CuteFilterChip(difficulty == option, option.label,
                                "difficulty_${option.name}") { difficulty = option }
                        }
                    }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(pluralStringResource(R.plurals.drawing_results, matches.size, matches.size),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f).testTag("result_count"))
                        AnimatedVisibility(
                            visible = query.isNotBlank() || category != null || difficulty != null,
                            enter = fadeIn(), exit = fadeOut(),
                        ) {
                            TextButton(onClick = clearFilters, modifier = Modifier.heightIn(min = 44.dp)) {
                                Icon(Icons.Default.Close, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(stringResource(R.string.clear_filters))
                            }
                        }
                    }
                }
            }
            if (matches.isEmpty()) {
                item(key = "empty", span = { GridItemSpan(maxLineSpan) }) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().testTag("explore_empty"),
                        shape = MaterialTheme.shapes.extraLarge,
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    ) {
                        Column(
                            Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            EmptyState(
                                title = stringResource(R.string.no_matching_drawings),
                                message = if (query.isNotBlank()) {
                                    stringResource(R.string.search_no_match, query.trim().take(60))
                                } else {
                                    stringResource(R.string.try_different_filters)
                                },
                                actionLabel = stringResource(R.string.clear_filters),
                                onAction = clearFilters,
                                modifier = Modifier.testTag("clear_filters_host"),
                                icon = null,
                                actionModifier = Modifier.testTag("clear_filters"),
                                iconVector = Icons.Default.Search,
                            )
                        }
                    }
                }
            }
            items(matches, key = { it.id }) { drawing ->
                DrawingCard(drawing, drawing.id in favorites, { onFavorite(drawing.id) }) {
                    focusManager.clearFocus()
                    onDrawing(drawing.id)
                }
            }
        }
    }
}

// ---------------------------------------------------------------- Detail

@Composable
fun DrawingDetailScreen(
    drawing: DrawingTemplate?,
    favorite: Boolean,
    onFavorite: () -> Unit,
    onExplore: () -> Unit = {},
    related: List<DrawingTemplate> = emptyList(),
    onRelated: (String) -> Unit = {},
    onTrace: () -> Unit,
) {
    ScreenColumn("screen_detail") {
        if (drawing == null) {
            EmptyState(
                title = stringResource(R.string.drawing_missing_title),
                message = stringResource(R.string.drawing_unavailable),
                actionLabel = stringResource(R.string.explore_drawings),
                onAction = onExplore,
            )
            return@ScreenColumn
        }
        val tint = artworkTint(drawing.category)
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            ArtworkPlate(
                drawing = drawing,
                modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth().aspectRatio(1f),
                targetSize = 1024,
                artworkPadding = 20.dp,
                contentDescription = stringResource(R.string.template_preview, drawing.name),
                shape = MaterialTheme.shapes.extraLarge,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(drawing.name, Modifier.weight(1f),
                style = MaterialTheme.typography.headlineLarge)
            FavoriteButton(drawing, favorite, onFavorite, Modifier.size(48.dp), onPlate = true,
                plateColor = MaterialTheme.colorScheme.surfaceContainerHigh)
        }
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Surface(shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer) {
                Text(
                    stringResource(R.string.template_metadata,
                        drawing.category.label, drawing.difficulty.label),
                    Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            DifficultyBars(drawing.difficulty, tint.accent)
        }
        Text(drawing.description, style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Button(
            onClick = onTrace,
            modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp).testTag("start_trace"),
            shape = MaterialTheme.shapes.large,
            contentPadding = PaddingValues(16.dp),
        ) {
            Icon(Icons.Default.Create, null, Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text(stringResource(R.string.open_trace), style = MaterialTheme.typography.titleMedium)
        }
        Text(stringResource(R.string.detail_tip), style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (related.isNotEmpty()) {
            SectionHeader(stringResource(R.string.related_drawings))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 4.dp),
            ) {
                items(related, key = { it.id }) { item ->
                    DrawingCard(
                        drawing = item,
                        favorite = false,
                        onFavorite = null,
                        modifier = Modifier.width(152.dp),
                        tag = "related_${item.id}",
                        showDifficulty = false,
                    ) { onRelated(item.id) }
                }
            }
        }
    }
}

// ---------------------------------------------------------------- Favorites

@Composable
fun FavoritesScreen(
    drawings: List<DrawingTemplate>,
    onFavorite: (String) -> Unit,
    onExplore: () -> Unit,
    onDrawing: (String) -> Unit,
) {
    DrawingGrid("screen_favorites") {
        fullWidth("intro") {
            Column(Modifier.padding(top = 8.dp, bottom = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)) {
                PageHeading(stringResource(R.string.favorites_title),
                    stringResource(R.string.favorites_subtitle))
                if (drawings.isNotEmpty()) {
                    Surface(shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer) {
                        Text(stringResource(R.string.favorites_count, drawings.size),
                            Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
        if (drawings.isEmpty()) {
            fullWidth("empty") {
                FavoritesEmptyState(onExplore, Modifier.testTag("favorites_empty"))
            }
        } else {
            items(drawings, key = { it.id }) { drawing ->
                DrawingCard(drawing, true, { onFavorite(drawing.id) }) { onDrawing(drawing.id) }
            }
        }
    }
}
