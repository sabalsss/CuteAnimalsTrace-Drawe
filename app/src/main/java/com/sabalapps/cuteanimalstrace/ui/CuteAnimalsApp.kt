package com.sabalapps.cuteanimalstrace.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import android.net.Uri
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sabalapps.cuteanimalstrace.data.UserPreferences
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.sabalapps.cuteanimalstrace.R
import com.sabalapps.cuteanimalstrace.data.Difficulty
import com.sabalapps.cuteanimalstrace.data.LocalTemplateCatalog
import com.sabalapps.cuteanimalstrace.data.RateSharePrompt
import com.sabalapps.cuteanimalstrace.notifications.DailyReminder
import com.sabalapps.cuteanimalstrace.data.TemplateCategory
import com.sabalapps.cuteanimalstrace.ui.navigation.DrawingDestination
import com.sabalapps.cuteanimalstrace.ui.navigation.TopLevelDestination

private const val TransitionMillis = 200

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CuteAnimalsApp(preferences: UserPreferences, model: UserPreferencesViewModel, catalog: LocalTemplateCatalog) {
    val snackbar = remember { SnackbarHostState() }
    val writeFailed by model.writeFailed.collectAsStateWithLifecycle()
    val errorText = stringResource(R.string.preferences_write_failed)
    LaunchedEffect(writeFailed) {
        if (writeFailed) {
            snackbar.showSnackbar(errorText)
            model.dismissWriteError()
        }
    }
    val favoriteSound = rememberFavoriteSound()
    val toggleFavorite: (String) -> Unit = { id ->
        if (id !in preferences.favorites) favoriteSound.play()
        model.update { toggleFavorite(id) }
    }
    val recordTraceSuccess: () -> Unit = { model.update { recordSuccessfulTrace() } }
    val navController = rememberNavController()
    val context = LocalContext.current
    // The daily nudge is scheduled locally by WorkManager; nothing is fetched or reported.
    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()) { /* Declining simply leaves it silent. */ }
    LaunchedEffect(preferences.dailyReminder) {
        if (preferences.dailyReminder) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
                androidx.core.content.ContextCompat.checkSelfPermission(
                    context, android.Manifest.permission.POST_NOTIFICATIONS) !=
                android.content.pm.PackageManager.PERMISSION_GRANTED) {
                notificationPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
            DailyReminder.schedule(context)
        } else {
            DailyReminder.cancel(context)
        }
    }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: SecurityException) { /* Temporary grants remain usable for this session. */ }
            navController.navigate("trace-image?uri=" + Uri.encode(uri.toString())) {
                if (navController.currentDestination?.route == "trace-image?uri={uri}") {
                    popUpTo("trace-image?uri={uri}") { inclusive = true }
                }
                launchSingleTop = true
            }
        }
    }
    val chooseImage: () -> Unit = {
        try { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
        catch (_: android.content.ActivityNotFoundException) {
            android.widget.Toast.makeText(context, R.string.picker_unavailable, android.widget.Toast.LENGTH_LONG).show()
        }
    }
    val entry by navController.currentBackStackEntryAsState()
    val route = entry?.destination?.route ?: TopLevelDestination.Home.route
    val topLevel = TopLevelDestination.entries.firstOrNull { it.route == route }
    val tracing = route == DrawingDestination.Trace || route.startsWith("trace-image")

    // The invitation waits until the user is back out of the camera, so it never covers tracing.
    var promptVisible by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(preferences.successfulTraceCount, preferences.hasRequestedReview,
        preferences.lastRateSharePromptTime, tracing) {
        if (!tracing && !promptVisible &&
            RateSharePrompt.shouldShow(preferences, System.currentTimeMillis())) {
            promptVisible = true
            model.update { recordPromptShown(System.currentTimeMillis()) }
        }
    }
    val shareMessage = stringResource(R.string.share_app_message)
    val shareTitle = stringResource(R.string.share_app)
    if (promptVisible) {
        RateShareDialog(
            onRate = {
                promptVisible = false
                model.update { recordReviewRequested() }
                launchInAppReview(context)
            },
            onShare = {
                promptVisible = false
                model.update { recordShared() }
                shareApp(context, shareMessage, shareTitle)
            },
            onDismiss = {
                promptVisible = false
                model.update { recordPromptDismissed() }
            },
        )
    }

    // Home's quick links; the featured rail never repeats inside the easy-to-trace rail.
    val categoryCounts = remember(catalog) {
        catalog.templates.groupingBy { it.category }.eachCount()
    }
    val easyPicks = remember(catalog) {
        val featured = catalog.featuredTemplates.map { it.id }.toSet()
        catalog.templates.filter { it.difficulty == Difficulty.Easy && it.id !in featured }.take(6)
    }
    var categoryRequest by rememberSaveable { mutableStateOf<TemplateCategory?>(null) }

    fun navigateToTab(destination: TopLevelDestination) {
        navController.navigate(destination.route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
    val openDrawing: (String) -> Unit = { id ->
        navController.navigate(DrawingDestination.detail(id)) { launchSingleTop = true }
    }

    // Settings can replay the walkthrough on top of the app, keeping navigation state underneath.
    var tutorialVisible by rememberSaveable { mutableStateOf(false) }
    val tutorialDrawings = remember(catalog) { catalog.tutorialDrawings() }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbar) },
            topBar = {
                // Every screen carries its own heading; only the detail route needs a bar for Back.
                if (topLevel == null && !tracing) {
                    TopAppBar(
                        title = {},
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                        ),
                    )
                }
            },
            bottomBar = {
                if (topLevel != null) {
                    NavigationBar(
                        modifier = Modifier.testTag("bottom_navigation"),
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    ) {
                        TopLevelDestination.entries.forEach { destination ->
                            NavigationBarItem(
                                modifier = Modifier.testTag("tab_${destination.route}"),
                                selected = topLevel == destination,
                                onClick = { navigateToTab(destination) },
                                icon = { Icon(destination.icon, contentDescription = null) },
                                label = { Text(stringResource(destination.label)) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                ),
                            )
                        }
                    }
                }
            },
        ) { padding ->
            // Only the tracing routes run full-bleed; everything else is inset per destination,
            // so a screen never jumps while a transition is still running.
            val inset: @Composable (@Composable () -> Unit) -> Unit = { content ->
                Box(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)) { content() }
            }
            NavHost(
                navController = navController,
                startDestination = TopLevelDestination.Home.route,
                modifier = Modifier.fillMaxSize(),
                enterTransition = { fadeIn(tween(TransitionMillis)) },
                exitTransition = { fadeOut(tween(TransitionMillis)) },
                popEnterTransition = { fadeIn(tween(TransitionMillis)) },
                popExitTransition = { fadeOut(tween(TransitionMillis)) },
            ) {
                composable(TopLevelDestination.Home.route) {
                    inset { HomeScreen(
                        drawings = catalog.featuredTemplates,
                        favorites = preferences.favorites,
                        recent = preferences.recent.mapNotNull(catalog::findById).take(6),
                        onFavorite = toggleFavorite,
                        onOwnImage = chooseImage,
                        easyPicks = easyPicks,
                        categoryCounts = categoryCounts,
                        onCategory = { category ->
                            categoryRequest = category
                            navigateToTab(TopLevelDestination.Explore)
                        },
                        onDrawing = openDrawing,
                    ) }
                }
                composable(TopLevelDestination.Explore.route) {
                    inset { ExploreScreen(
                        drawings = catalog.templates,
                        favorites = preferences.favorites,
                        onFavorite = toggleFavorite,
                        categoryRequest = categoryRequest,
                        onCategoryHandled = { categoryRequest = null },
                        onDrawing = openDrawing,
                    ) }
                }
                composable(TopLevelDestination.Favorites.route) {
                    inset { FavoritesScreen(catalog.templates.filter { it.id in preferences.favorites },
                        onFavorite = toggleFavorite,
                        onExplore = { navigateToTab(TopLevelDestination.Explore) },
                        onDrawing = openDrawing) }
                }
                composable(TopLevelDestination.Settings.route) {
                    inset { SettingsScreen(preferences, onUpdate = model::update, onShowTutorial = { tutorialVisible = true }) }
                }
                composable(DrawingDestination.Detail,
                    arguments = listOf(navArgument(DrawingDestination.Argument) { type = NavType.StringType }),
                ) { backStackEntry ->
                    val drawing = catalog.findById(backStackEntry.arguments?.getString(DrawingDestination.Argument))
                    LaunchedEffect(backStackEntry.id) {
                        drawing?.let { model.update { recordViewed(it.id) } }
                    }
                    val related = remember(drawing, catalog) {
                        drawing?.let { current ->
                            catalog.templates.filter { it.category == current.category && it.id != current.id }
                                .take(8)
                        }.orEmpty()
                    }
                    inset {
                        DrawingDetailScreen(drawing, drawing?.id in preferences.favorites,
                            onFavorite = { drawing?.let { toggleFavorite(it.id) } },
                            onExplore = { navigateToTab(TopLevelDestination.Explore) },
                            related = related,
                            onRelated = openDrawing) {
                            drawing?.let { navController.navigate(DrawingDestination.trace(it.id)) { launchSingleTop = true } }
                        }
                    }
                }
                composable("trace-image?uri={uri}",
                    arguments = listOf(navArgument("uri") { type = NavType.StringType; defaultValue = "" }),
                    enterTransition = { fadeIn(tween(TransitionMillis)) + slideInVertically { it / 12 } },
                    popExitTransition = { fadeOut(tween(TransitionMillis)) + slideOutVertically { it / 12 } },
                ) { backStackEntry ->
                    TraceScreen(null, preferences,
                        imageUri = backStackEntry.arguments?.getString("uri"),
                        onChooseImage = chooseImage,
                        onExplore = { navigateToTab(TopLevelDestination.Explore) },
                        onTraceSuccess = recordTraceSuccess,
                        onBack = { navController.popBackStack() })
                }
                composable(DrawingDestination.Trace,
                    arguments = listOf(navArgument(DrawingDestination.Argument) { type = NavType.StringType }),
                    enterTransition = { fadeIn(tween(TransitionMillis)) + slideInVertically { it / 12 } },
                    popExitTransition = { fadeOut(tween(TransitionMillis)) + slideOutVertically { it / 12 } },
                ) { backStackEntry ->
                    val drawing = catalog.findById(backStackEntry.arguments?.getString(DrawingDestination.Argument))
                    TraceScreen(drawing, preferences,
                        onExplore = { navigateToTab(TopLevelDestination.Explore) },
                        onTraceSuccess = recordTraceSuccess,
                        onBack = { navController.popBackStack() })
                }
            }
        }
        if (tutorialVisible) {
            OnboardingScreen(tutorialDrawings, onFinish = { tutorialVisible = false }, handleBack = true)
        }
    }
}
