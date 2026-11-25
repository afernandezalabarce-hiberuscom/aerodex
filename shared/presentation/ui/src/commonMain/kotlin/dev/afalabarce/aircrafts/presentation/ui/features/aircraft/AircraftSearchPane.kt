package dev.afalabarce.aircrafts.presentation.ui.features.aircraft

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AirplanemodeInactive
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import dev.afalabarce.aircrafts.core.ui.theme.AppMaterialTheme
import dev.afalabarce.aircrafts.domain.models.AircraftCategory
import dev.afalabarce.aircrafts.domain.models.AircraftModelSummary
import dev.afalabarce.aircrafts.domain.models.DataSourceOrigin
import dev.afalabarce.aircrafts.presentation.ui.features.aircraft.components.AeroDexSearchBar
import dev.afalabarce.aircrafts.presentation.ui.features.aircraft.components.AeroDexTopBar
import dev.afalabarce.aircrafts.presentation.ui.features.aircraft.components.AircraftCard
import dev.afalabarce.aircrafts.presentation.ui.features.aircraft.components.CategoryFilterChips
import dev.afalabarce.aircrafts.presentation.ui.features.aircraft.components.OfflineBanner
import dev.afalabarce.aircrafts.presentation.ui.features.aircraft.components.rememberLazyGridScrollDownState
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.Res
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.error_compare_failed
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.error_detail_failed
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.error_loading
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.error_search_failed
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.no_results
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.no_results_hint
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.retry
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.searching_aircraft
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.showing_aircraft_count
import dev.afalabarce.aircrafts.presentation.viewmodels.features.aircraft.AircraftError
import dev.afalabarce.aircrafts.presentation.viewmodels.features.aircraft.AircraftUiState
import org.jetbrains.compose.resources.stringResource

private object SearchPaneTokens {
    const val SINGLE_COLUMN = 1
    const val TWO_COLUMNS = 2
    const val LOAD_MORE_PREFETCH_THRESHOLD = 4
    const val INITIAL_VISIBLE_INDEX = 0
    const val MIN_INDEX = 0
    const val AIRCRAFT_CARD_CONTENT_TYPE = "aircraft_card"
    const val LOADING_FOOTER_CONTENT_TYPE = "loading_footer"
}

@Composable
internal fun AircraftError.toDisplayMessage(): String = when (this) {
    is AircraftError.Message -> detail
    AircraftError.SearchFailed -> stringResource(Res.string.error_search_failed)
    AircraftError.DetailFailed -> stringResource(Res.string.error_detail_failed)
    AircraftError.CompareFailed -> stringResource(Res.string.error_compare_failed)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AircraftSearchPane(
    uiState: AircraftUiState,
    onQueryChange: (String) -> Unit,
    onSearchSubmit: () -> Unit,
    onClearQuery: () -> Unit,
    onCategorySelected: (AircraftCategory?) -> Unit,
    onAircraftClick: (String) -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    bottomContentPadding: Dp = AppMaterialTheme.dimens.none,
    onToggleFavorite: (AircraftModelSummary) -> Unit = {},
    onToggleCompare: (String) -> Unit = {},
    onScrollDirectionChanged: (Boolean) -> Unit = {}
) {
    val dimens = AppMaterialTheme.dimens

    Column(modifier = modifier.fillMaxSize()) {
        AeroDexTopBar(origin = uiState.searchOrigin)

        AeroDexSearchBar(
            query = uiState.query,
            onQueryChange = onQueryChange,
            onSearchSubmit = onSearchSubmit,
            onClear = onClearQuery,
            modifier = Modifier.padding(horizontal = dimens.appMargin)
        )

        Spacer(modifier = Modifier.height(dimens.spacingMd))

        CategoryFilterChips(
            selectedCategory = uiState.selectedCategory,
            onCategorySelected = onCategorySelected
        )

        if (uiState.searchOrigin == DataSourceOrigin.OFFLINE_CACHE) {
            Spacer(modifier = Modifier.height(dimens.spacingMd))
            OfflineBanner()
        }

        Spacer(modifier = Modifier.height(dimens.minTopSurface))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimens.appMargin, vertical = dimens.spacingXs)
                .semantics { liveRegion = LiveRegionMode.Polite },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (uiState.isLoading && uiState.items.isEmpty()) {
                    stringResource(Res.string.searching_aircraft)
                } else {
                    stringResource(Res.string.showing_aircraft_count, uiState.items.size)
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (uiState.isLoading && uiState.items.isNotEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.size(dimens.progressIndicatorSm),
                    strokeWidth = dimens.thinBorder
                )
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when {
                uiState.isLoading && uiState.items.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingIndicator()
                    }
                }

                uiState.searchError != null && uiState.items.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(dimens.mediumMarginStartSurface)
                            .semantics { liveRegion = LiveRegionMode.Polite },
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CloudOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(dimens.icon2Xl)
                        )
                        Spacer(modifier = Modifier.height(dimens.spacingLg))
                        Text(
                            text = stringResource(Res.string.error_loading),
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(dimens.spacingSm))
                        Text(
                            text = uiState.searchError?.toDisplayMessage().orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(dimens.topSurface))
                        Button(onClick = onRetry) {
                            Text(stringResource(Res.string.retry))
                        }
                    }
                }

                uiState.items.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(dimens.mediumMarginStartSurface),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AirplanemodeInactive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(dimens.icon2Xl)
                        )
                        Spacer(modifier = Modifier.height(dimens.spacingLg))
                        Text(
                            text = stringResource(Res.string.no_results),
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(dimens.spacingXs))
                        Text(
                            text = stringResource(Res.string.no_results_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                else -> {
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val columns = if (maxWidth >= dimens.twoPaneGridBreakpoint) {
                            SearchPaneTokens.TWO_COLUMNS
                        } else {
                            SearchPaneTokens.SINGLE_COLUMN
                        }
                        val gridState = rememberLazyGridState()
                        val isScrollingDown = rememberLazyGridScrollDownState(gridState)
                        LaunchedEffect(isScrollingDown) {
                            onScrollDirectionChanged(isScrollingDown)
                        }
                        val shouldLoadMore by remember(uiState.items.size, uiState.hasMore) {
                            derivedStateOf {
                                val lastVisible = gridState.layoutInfo.visibleItemsInfo
                                    .lastOrNull()?.index ?: SearchPaneTokens.INITIAL_VISIBLE_INDEX
                                uiState.hasMore &&
                                    lastVisible >= (uiState.items.size - SearchPaneTokens.LOAD_MORE_PREFETCH_THRESHOLD)
                                        .coerceAtLeast(SearchPaneTokens.MIN_INDEX)
                            }
                        }

                        LaunchedEffect(shouldLoadMore) {
                            if (shouldLoadMore) {
                                onLoadMore()
                            }
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
                                items = uiState.items,
                                key = { it.icaoTypeCode },
                                contentType = { SearchPaneTokens.AIRCRAFT_CARD_CONTENT_TYPE }
                            ) { aircraft ->
                                val normalizedCode = remember(aircraft.icaoTypeCode) {
                                    aircraft.icaoTypeCode.uppercase()
                                }
                                AircraftCard(
                                    aircraft = aircraft,
                                    isSelected = aircraft.icaoTypeCode == uiState.selectedIcaoCode,
                                    isFavorite = normalizedCode in uiState.favoriteCodes,
                                    isCompared = normalizedCode in uiState.compareCodes,
                                    onToggleFavorite = { onToggleFavorite(aircraft) },
                                    onToggleCompare = { onToggleCompare(aircraft.icaoTypeCode) },
                                    onClick = { onAircraftClick(aircraft.icaoTypeCode) }
                                )
                            }

                            if (uiState.isLoadingMore) {
                                item(
                                    span = { GridItemSpan(columns) },
                                    contentType = SearchPaneTokens.LOADING_FOOTER_CONTENT_TYPE
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(dimens.appMargin),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(dimens.progressIndicatorMd)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
