package dev.afalabarce.aircrafts.presentation.ui.common

import androidx.compose.runtime.Composable

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS handles back navigation via the top bar navigation button
}
