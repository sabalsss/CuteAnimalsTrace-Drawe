package com.sabalapps.cuteanimalstrace.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.sabalapps.cuteanimalstrace.R

// Only these four routes belong to the persistent top-level tab stacks.
enum class TopLevelDestination(val route: String, @param:StringRes val label: Int, val icon: ImageVector) {
    Home("home", R.string.home, Icons.Default.Home),
    Explore("explore", R.string.explore, Icons.Default.Search),
    Favorites("favorites", R.string.favorites, Icons.Default.Favorite),
    Settings("settings", R.string.settings, Icons.Default.Settings),
}

object DrawingDestination {
    const val Argument = "drawingId"
    const val Detail = "drawing/{$Argument}"
    const val Trace = "trace/{$Argument}"
    fun detail(id: String) = "drawing/$id"
    fun trace(id: String) = "trace/$id"
}
