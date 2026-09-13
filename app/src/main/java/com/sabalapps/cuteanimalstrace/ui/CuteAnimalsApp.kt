package com.sabalapps.cuteanimalstrace.ui

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.sabalapps.cuteanimalstrace.R
import com.sabalapps.cuteanimalstrace.data.LocalTemplateCatalog
import com.sabalapps.cuteanimalstrace.ui.navigation.DrawingDestination
import com.sabalapps.cuteanimalstrace.ui.navigation.TopLevelDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CuteAnimalsApp() {
    val navController = rememberNavController()
    val entry by navController.currentBackStackEntryAsState()
    val route = entry?.destination?.route ?: TopLevelDestination.Home.route
    val topLevel = TopLevelDestination.entries.firstOrNull { it.route == route }
    val title = topLevel?.label ?: if (route == DrawingDestination.Trace) R.string.trace else R.string.drawing_detail

    fun navigateToTab(destination: TopLevelDestination) {
        navController.navigate(destination.route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(title)) },
                navigationIcon = {
                    if (topLevel == null) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
                        }
                    }
                },
            )
        },
        bottomBar = {
            if (topLevel != null) {
                NavigationBar(modifier = Modifier.testTag("bottom_navigation")) {
                    TopLevelDestination.entries.forEach { destination ->
                        NavigationBarItem(
                            modifier = Modifier.testTag("tab_${destination.route}"),
                            selected = topLevel == destination,
                            onClick = { navigateToTab(destination) },
                            icon = { Icon(destination.icon, contentDescription = null) },
                            label = { Text(stringResource(destination.label)) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = TopLevelDestination.Home.route,
            modifier = Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding),
        ) {
            composable(TopLevelDestination.Home.route) {
                HomeScreen(
                    drawings = LocalTemplateCatalog.featuredTemplates,
                    onExplore = { navigateToTab(TopLevelDestination.Explore) },
                    onDrawing = { navController.navigate(DrawingDestination.detail(it)) },
                )
            }
            composable(TopLevelDestination.Explore.route) {
                ExploreScreen(LocalTemplateCatalog.templates) { navController.navigate(DrawingDestination.detail(it)) }
            }
            composable(TopLevelDestination.Favorites.route) { FavoritesScreen() }
            composable(TopLevelDestination.Settings.route) { SettingsScreen() }
            composable(DrawingDestination.Detail,
                arguments = listOf(navArgument(DrawingDestination.Argument) { type = NavType.StringType }),
            ) { backStackEntry ->
                val drawing = LocalTemplateCatalog.findById(backStackEntry.arguments?.getString(DrawingDestination.Argument))
                DrawingDetailScreen(drawing) {
                    drawing?.let { navController.navigate(DrawingDestination.trace(it.id)) { launchSingleTop = true } }
                }
            }
            composable(DrawingDestination.Trace,
                arguments = listOf(navArgument(DrawingDestination.Argument) { type = NavType.StringType }),
            ) { backStackEntry ->
                val drawing = LocalTemplateCatalog.findById(backStackEntry.arguments?.getString(DrawingDestination.Argument))
                TraceScreen(drawing)
            }
        }
    }
}
