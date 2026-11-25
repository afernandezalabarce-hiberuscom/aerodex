package dev.afalabarce.aircrafts.presentation.ui.common

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker

@Composable
actual fun rememberHingeBounds(): HingeBounds? {
    val view = LocalView.current
    if (view.isInEditMode) return null

    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() } ?: return null
    val layoutInfoFlow = remember(activity) {
        WindowInfoTracker.getOrCreate(activity).windowLayoutInfo(activity)
    }
    val layoutInfo by layoutInfoFlow.collectAsState(initial = null)
    val foldingFeature = layoutInfo
        ?.displayFeatures
        ?.filterIsInstance<FoldingFeature>()
        ?.firstOrNull()
        ?: return null

    val density = LocalDensity.current
    val bounds = foldingFeature.bounds
    return with(density) {
        HingeBounds(
            left = bounds.left.toDp(),
            top = bounds.top.toDp(),
            right = bounds.right.toDp(),
            bottom = bounds.bottom.toDp(),
            width = bounds.width().toDp(),
            height = bounds.height().toDp(),
            orientation = if (foldingFeature.orientation == FoldingFeature.Orientation.VERTICAL) {
                HingeOrientation.VERTICAL
            } else {
                HingeOrientation.HORIZONTAL
            },
            isSeparating = foldingFeature.isSeparating
        )
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
