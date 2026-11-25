package dev.afalabarce.aircrafts

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import dev.afalabarce.aircrafts.core.common.platform.AndroidPlatform
import dev.afalabarce.aircrafts.presentation.ui.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AndroidPlatform.androidContext = this

        setContent {
            val localView = LocalView.current

            val isDarkMode = isSystemInDarkTheme()

            if (!localView.isInEditMode) {
                val window = (localView.context as Activity).window

                WindowCompat.getInsetsController(window, localView).isAppearanceLightStatusBars = !isDarkMode
            }
            App()
        }
    }
}
