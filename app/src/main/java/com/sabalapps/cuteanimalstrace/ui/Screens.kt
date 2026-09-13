package com.sabalapps.cuteanimalstrace.ui

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sabalapps.cuteanimalstrace.R

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

@Composable
private fun DrawingCard(drawing: PlaceholderDrawing, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth().testTag("drawing_${drawing.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Illustration(coral = drawing.id == "kitten" || drawing.id == "fox")
            Text(stringResource(drawing.name), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.sample_drawing), style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun HomeScreen(onExplore: () -> Unit, onDrawing: (String) -> Unit) {
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
        DrawingCard(placeholderDrawings.first()) { onDrawing(placeholderDrawings.first().id) }
    }
}

@Composable
fun ExploreScreen(onDrawing: (String) -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(156.dp),
            modifier = Modifier.widthIn(max = 960.dp).fillMaxSize().testTag("screen_explore"),
            contentPadding = PaddingValues(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Intro(stringResource(R.string.explore_title), stringResource(R.string.explore_subtitle))
            }
            items(placeholderDrawings, key = { it.id }) { drawing ->
                DrawingCard(drawing) { onDrawing(drawing.id) }
            }
        }
    }
}

@Composable
fun DrawingDetailScreen(drawing: PlaceholderDrawing?, onTrace: () -> Unit) {
    ScreenColumn("screen_detail") {
        if (drawing == null) {
            Text(stringResource(R.string.drawing_unavailable))
        } else {
            Illustration(coral = true)
            Intro(stringResource(drawing.name), stringResource(R.string.detail_subtitle))
            Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.secondaryContainer) {
                Text(stringResource(R.string.sample_drawing), Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge)
            }
            Text(stringResource(R.string.detail_description), style = MaterialTheme.typography.bodyLarge)
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
fun TraceScreen(drawing: PlaceholderDrawing?) {
    ScreenColumn("screen_trace") {
        Illustration(Icons.Default.Create)
        Intro(stringResource(R.string.trace_title),
            drawing?.let { stringResource(R.string.trace_selected, stringResource(it.name)) }
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
