package com.sabalapps.cuteanimalstrace.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.res.pluralStringResource
import com.sabalapps.cuteanimalstrace.data.Difficulty
import com.sabalapps.cuteanimalstrace.data.TemplateCategory
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sabalapps.cuteanimalstrace.R
import com.sabalapps.cuteanimalstrace.data.DrawingTemplate

@Composable
private fun ScreenColumn(tag: String, content: @Composable ColumnScope.() -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier.widthIn(max = 720.dp).fillMaxWidth().testTag(tag)
                .verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            content = content,
        )
    }
}

@Composable
private fun Intro(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.headlineLarge)
        Text(subtitle, style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun Illustration(icon: ImageVector = Icons.Default.Face, coral: Boolean = false) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(152.dp),
        shape = MaterialTheme.shapes.large,
        color = if (coral) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer,
        contentColor = if (coral) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        Box(contentAlignment = Alignment.Center) { Icon(icon, null, Modifier.size(64.dp)) }
    }
}

/** Artwork stays on light paper in both themes, preserving the contrast of its ink. */
@Composable
private fun TemplateImage(drawing: DrawingTemplate, expanded: Boolean = false) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(if (expanded) 280.dp else 152.dp),
        shape = MaterialTheme.shapes.large,
        color = Color(0xFFFFFCF7),
    ) {
        Image(
            painter = painterResource(drawing.imageRes),
            contentDescription = stringResource(R.string.template_preview, drawing.name),
            modifier = Modifier.padding(16.dp).testTag("template_image_${drawing.id}"),
            contentScale = ContentScale.Fit,
        )
    }
}

@Composable
private fun DrawingCard(drawing: DrawingTemplate, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth().testTag("drawing_${drawing.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TemplateImage(drawing)
            Text(drawing.name, style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.template_metadata, drawing.category.label, drawing.difficulty.label), style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun HomeScreen(drawings: List<DrawingTemplate>, onExplore: () -> Unit, onDrawing: (String) -> Unit) {
    ScreenColumn("screen_home") {
        Intro(stringResource(R.string.home_greeting), stringResource(R.string.home_subtitle))
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Icon(Icons.Default.Create, null, Modifier.size(32.dp))
                Text(stringResource(R.string.hero_title), style = MaterialTheme.typography.headlineSmall)
                Text(stringResource(R.string.hero_description), style = MaterialTheme.typography.bodyLarge)
                Button(onClick = onExplore) { Text(stringResource(R.string.explore_drawings)) }
            }
        }
        Text(stringResource(R.string.little_inspiration), style = MaterialTheme.typography.titleLarge)
        drawings.forEach { drawing -> DrawingCard(drawing) { onDrawing(drawing.id) } }
    }
}

@Composable
fun ExploreScreen(drawings: List<DrawingTemplate>, onDrawing: (String) -> Unit) {
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

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(156.dp),
            modifier = Modifier.widthIn(max = 960.dp).fillMaxSize().imePadding().testTag("screen_explore"),
            contentPadding = PaddingValues(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item(key = "filters", span = { GridItemSpan(maxLineSpan) }) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Intro(stringResource(R.string.explore_title), stringResource(R.string.explore_subtitle))
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth().testTag("explore_search"),
                        label = { Text(stringResource(R.string.search_drawings)) },
                        placeholder = { Text(stringResource(R.string.search_hint)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { query = "" }) {
                                    Icon(Icons.Default.Close, stringResource(R.string.clear_search))
                                }
                            }
                        },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                    )
                    Text(stringResource(R.string.categories), style = MaterialTheme.typography.titleMedium)
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = category == null, onClick = { category = null },
                            label = { Text(stringResource(R.string.all_categories)) },
                            modifier = Modifier.testTag("category_all"))
                        TemplateCategory.entries.forEach { option ->
                            FilterChip(selected = category == option, onClick = { category = option },
                                label = { Text(option.label) },
                                modifier = Modifier.testTag("category_${option.name}"))
                        }
                    }
                    Text(stringResource(R.string.difficulty), style = MaterialTheme.typography.titleMedium)
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = difficulty == null, onClick = { difficulty = null },
                            label = { Text(stringResource(R.string.all_difficulties)) },
                            modifier = Modifier.testTag("difficulty_all"))
                        Difficulty.entries.forEach { option ->
                            FilterChip(selected = difficulty == option, onClick = { difficulty = option },
                                label = { Text(option.label) },
                                modifier = Modifier.testTag("difficulty_${option.name}"))
                        }
                    }
                    Text(pluralStringResource(R.plurals.drawing_results, matches.size, matches.size),
                        style = MaterialTheme.typography.labelLarge, modifier = Modifier.testTag("result_count"))
                }
            }
            if (matches.isEmpty()) {
                item(key = "empty", span = { GridItemSpan(maxLineSpan) }) {
                    Card(Modifier.fillMaxWidth().testTag("explore_empty")) {
                        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Default.Search, contentDescription = null)
                            Text(stringResource(R.string.no_matching_drawings), style = MaterialTheme.typography.titleLarge)
                            Text(stringResource(R.string.try_different_filters))
                            Button(onClick = clearFilters, modifier = Modifier.testTag("clear_filters")) {
                                Text(stringResource(R.string.clear_filters))
                            }
                        }
                    }
                }
            }
            items(matches, key = { it.id }) { drawing ->
                DrawingCard(drawing) {
                    focusManager.clearFocus()
                    onDrawing(drawing.id)
                }
            }
        }
    }
}

@Composable
fun DrawingDetailScreen(drawing: DrawingTemplate?, onTrace: () -> Unit) {
    ScreenColumn("screen_detail") {
        if (drawing == null) {
            Text(stringResource(R.string.drawing_unavailable))
        } else {
            TemplateImage(drawing, expanded = true)
            Text(drawing.name, style = MaterialTheme.typography.headlineLarge)
            Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.secondaryContainer) {
                Text(stringResource(R.string.template_metadata, drawing.category.label, drawing.difficulty.label), Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge)
            }
            Text(drawing.description, style = MaterialTheme.typography.bodyLarge)
            Button(onClick = onTrace, modifier = Modifier.fillMaxWidth().testTag("start_trace"),
                contentPadding = PaddingValues(16.dp)) {
                Icon(Icons.Default.Create, null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.open_trace))
            }
        }
    }
}

@Composable
fun TraceScreen(drawing: DrawingTemplate?) {
    ScreenColumn("screen_trace") {
        Illustration(Icons.Default.Create)
        Intro(stringResource(R.string.trace_title),
            drawing?.let { stringResource(R.string.trace_selected, it.name) }
                ?: stringResource(R.string.drawing_unavailable))
        Text(stringResource(R.string.trace_placeholder), style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun FavoritesScreen() {
    ScreenColumn("screen_favorites") {
        Illustration(Icons.Default.Favorite, coral = true)
        Intro(stringResource(R.string.favorites_title), stringResource(R.string.favorites_subtitle))
        Text(stringResource(R.string.favorites_placeholder), style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun SettingsScreen() {
    ScreenColumn("screen_settings") {
        Intro(stringResource(R.string.settings_title), stringResource(R.string.settings_subtitle))
        Card {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.appearance), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.follows_system), style = MaterialTheme.typography.bodyLarge)
                HorizontalDivider()
                Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.settings_placeholder), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
