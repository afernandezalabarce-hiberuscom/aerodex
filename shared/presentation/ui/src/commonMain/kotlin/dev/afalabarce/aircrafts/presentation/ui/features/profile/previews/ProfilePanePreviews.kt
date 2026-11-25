package dev.afalabarce.aircrafts.presentation.ui.features.profile.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.afalabarce.aircrafts.core.ui.theme.AppMaterialTheme
import dev.afalabarce.aircrafts.presentation.ui.features.aircraft.components.AeroDexFloatingBarClearance
import dev.afalabarce.aircrafts.presentation.ui.features.profile.screen.ProfilePane

@Preview
@Composable
fun ProfilePaneCompactPreview() {
    AppMaterialTheme {
        ProfilePane(
            bottomContentPadding = AeroDexFloatingBarClearance
        )
    }
}

@Preview
@Composable
fun ProfilePaneExpandedPreview() {
    AppMaterialTheme {
        ProfilePane(
            bottomContentPadding = 0.dp
        )
    }
}
