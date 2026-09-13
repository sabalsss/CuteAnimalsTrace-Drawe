package com.sabalapps.cuteanimalstrace

import android.os.Bundle
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
            CuteAnimalsTheme { CuteAnimalsApp() }
        }
    }
}
