package dev.afalabarce.aircrafts.presentation.ui.features.aircraft

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import dev.afalabarce.aircrafts.core.ui.theme.AppMaterialTheme
import dev.afalabarce.aircrafts.domain.models.AircraftModelSummary
import dev.afalabarce.aircrafts.presentation.ui.features.aircraft.components.AeroDexTopBar
import dev.afalabarce.aircrafts.presentation.ui.features.aircraft.components.AircraftCard
import dev.afalabarce.aircrafts.presentation.ui.features.aircraft.components.rememberLazyGridScrollDownState
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.Res
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.favorites_empty_subtitle
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.favorites_empty_title
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.favorites_title_count
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.nav_favorites_cd
import dev.afalabarce.aircrafts.presentation.viewmodels.features.aircraft.AircraftUiState
import org.jetbrains.compose.resources.stringResource

private object FavoritesPaneTokens {
    const val SINGLE_COLUMN = 1
    const val TWO_COLUMNS = 2
    const val AIRCRAFT_CARD_CONTENT_TYPE = "aircraft_card"
}

@Composable
fun FavoritesPane(
    uiState: AircraftUiState,
    bottomContentPadding: Dp,
    onAircraftClick: (String) -> Unit,
    onToggleFavorite: (AircraftModelSummary) -> Unit,
    onToggleCompare: (String) -> Unit,
    modifier: Modifier = Modifier,
    onScrollDirectionChanged: (Boolean) -> Unit = {}
) {
    val dimens = AppMaterialTheme.dimens

    Column(modifier = modifier.fillMaxSize()) {
        AeroDexTopBar(
            origin = null,
            subtitle = stringResource(Res.string.nav_favorites_cd)
        )

        Text(
            text = stringResource(Res.string.favorites_title_count, uiState.favorites.size),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .padding(horizontal = dimens.appMargin, vertical = dimens.spacingXs)
                .semantics { heading() }
        )

        if (uiState.favorites.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(dimens.mediumMarginStartSurface)
                    .padding(bottom = bottomContentPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Rounded.FavoriteBorder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(dimens.icon3Xl)
                )
                Spacer(modifier = Modifier.height(dimens.spacingLg))
                Text(
                    text = stringResource(Res.string.favorites_empty_title),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(dimens.spacingSm))
                Text(
                    text = stringResource(Res.string.favorites_empty_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth()) {
                val columns = if (maxWidth >= dimens.twoPaneGridBreakpoint) {
                    FavoritesPaneTokens.TWO_COLUMNS
                } else {
                    FavoritesPaneTokens.SINGLE_COLUMN
                }
                val gridState = rememberLazyGridState()
                val isScrollingDown = rememberLazyGridScrollDownState(gridState)
                LaunchedEffect(isScrollingDown) {
                    onScrollDirectionChanged(isScrollingDown)
                }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    state = gridState,
                    contentPadding = PaddingValues(
                        start = dimens.appMargin,
                        end = dimens.appMargin,
                        top = dimens.spacingLg,
                        bottom = dimens.bottomSurface + bottomContentPadding
                    ),
                    horizontalArrangement = Arrangement.spacedBy(dimens.spacingLg),
                    verticalArrangement = Arrangement.spacedBy(dimens.spacingLg),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = uiState.favorites,
                        key = { it.icaoTypeCode },
                        contentType = { FavoritesPaneTokens.AIRCRAFT_CARD_CONTENT_TYPE }
                    ) { aircraft ->
                        val normalizedCode = remember(aircraft.icaoTypeCode) {
                            aircraft.icaoTypeCode.uppercase()
                        }
                        AircraftCard(
                            aircraft = aircraft,
                            isSelected = aircraft.icaoTypeCode == uiState.selectedIcaoCode,
                            isFavorite = true,
                            isCompared = normalizedCode in uiState.compareCodes,
                            onToggleFavorite = { onToggleFavorite(aircraft) },
                            onToggleCompare = { onToggleCompare(aircraft.icaoTypeCode) },
                            onClick = { onAircraftClick(aircraft.icaoTypeCode) }
                        )
                    }
                }
            }
        }
    }
}
