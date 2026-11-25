package dev.afalabarce.aircrafts.presentation.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp

enum class HingeOrientation {
    VERTICAL,
    HORIZONTAL
}

data class HingeBounds(
    val left: Dp,
    val top: Dp,
    val right: Dp,
    val bottom: Dp,
    val width: Dp,
    val height: Dp,
    val orientation: HingeOrientation,
    val isSeparating: Boolean
)

@Composable
expect fun rememberHingeBounds(): HingeBounds?
