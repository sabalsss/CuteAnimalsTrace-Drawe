package com.sabalapps.cuteanimalstrace

import android.os.Bundle
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.SideEffect
import androidx.core.view.WindowCompat
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sabalapps.cuteanimalstrace.data.Appearance
import com.sabalapps.cuteanimalstrace.ui.UserPreferencesViewModel
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sabalapps.cuteanimalstrace.ui.AppSplash
import com.sabalapps.cuteanimalstrace.ui.CuteAnimalsApp
import com.sabalapps.cuteanimalstrace.ui.OnboardingScreen
import com.sabalapps.cuteanimalstrace.ui.tutorialDrawings
import com.sabalapps.cuteanimalstrace.ui.theme.CuteAnimalsTheme

/** Launch flow: splash while data loads, the walkthrough on first run, then the app. */
private enum class Stage { Splash, Failed, Tutorial, App }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val model: UserPreferencesViewModel = viewModel()
            val catalog by model.catalog.collectAsStateWithLifecycle()
            val preferences by model.preferences.collectAsStateWithLifecycle()
            val loadFailed by model.loadFailed.collectAsStateWithLifecycle()
            val settings = preferences
            val templates = catalog
            val dark = when (settings?.appearance ?: Appearance.System) {
                Appearance.System -> isSystemInDarkTheme()
                Appearance.Light -> false
                Appearance.Dark -> true
            }
            SideEffect {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !dark
                    isAppearanceLightNavigationBars = !dark
                }
            }
            CuteAnimalsTheme(darkTheme = dark, dynamicColor = settings?.dynamicColor ?: false) {
                // Shown once per launch; rotation keeps it finished instead of replaying it.
                var splashDone by rememberSaveable { mutableStateOf(false) }
                Crossfade(targetState = when {
                    loadFailed && (settings == null || templates == null) -> Stage.Failed
                    !splashDone || settings == null || templates == null -> Stage.Splash
                    !settings.onboardingSeen -> Stage.Tutorial
                    else -> Stage.App
                }, animationSpec = tween(350), label = "launchStage") { stage ->
                    when (stage) {
                        Stage.Splash -> AppSplash(ready = settings != null && templates != null,
                            onFinished = { splashDone = true })
                        Stage.Failed -> Surface(Modifier.fillMaxSize()) {
                            Box(contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(stringResource(R.string.preferences_load_failed))
                                    Button(onClick = model::load) { Text(stringResource(R.string.retry_camera)) }
                                }
                            }
                        }
                        Stage.Tutorial -> if (templates != null) {
                            OnboardingScreen(remember(templates) { templates.tutorialDrawings() },
                                onFinish = { model.update { setOnboardingSeen() } })
                        }
                        Stage.App -> if (settings != null && templates != null) {
                            CuteAnimalsApp(settings, model, templates)
                        }
                    }
                }
            }
        }
    }
}
