package com.sabalapps.cuteanimalstrace

import android.os.Bundle
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
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
import com.sabalapps.cuteanimalstrace.ui.CuteAnimalsApp
import com.sabalapps.cuteanimalstrace.ui.theme.CuteAnimalsTheme

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
                if (settings == null || templates == null) {
                    Surface(Modifier.fillMaxSize()) {
                        Box(contentAlignment = Alignment.Center) {
                            if (loadFailed) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(stringResource(R.string.preferences_load_failed))
                                    Button(onClick = model::load) { Text(stringResource(R.string.retry_camera)) }
                                }
                            } else CircularProgressIndicator()
                        }
                    }
                } else CuteAnimalsApp(settings, model, templates)
            }
        }
    }
}
