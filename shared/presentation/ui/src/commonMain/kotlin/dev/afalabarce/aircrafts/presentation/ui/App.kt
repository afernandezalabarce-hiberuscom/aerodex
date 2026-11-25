package dev.afalabarce.aircrafts.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.CompareArrows
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import dev.afalabarce.aircrafts.core.ui.theme.AppMaterialTheme
import dev.afalabarce.aircrafts.presentation.ui.common.HingeBounds
import dev.afalabarce.aircrafts.presentation.ui.common.HingeOrientation
import dev.afalabarce.aircrafts.presentation.ui.common.PlatformBackHandler
import dev.afalabarce.aircrafts.presentation.ui.common.rememberHingeBounds
import dev.afalabarce.aircrafts.presentation.ui.features.aircraft.AircraftDetailPane
import dev.afalabarce.aircrafts.presentation.ui.features.aircraft.AircraftSearchPane
import dev.afalabarce.aircrafts.presentation.ui.features.aircraft.ComparatorPane
import dev.afalabarce.aircrafts.presentation.ui.features.aircraft.EmptyDetailPlaceholder
import dev.afalabarce.aircrafts.presentation.ui.features.aircraft.FavoritesPane
import dev.afalabarce.aircrafts.presentation.ui.features.aircraft.components.AeroDexFloatingBarClearance
import dev.afalabarce.aircrafts.presentation.ui.features.aircraft.components.AeroDexNavigationBar
import dev.afalabarce.aircrafts.presentation.ui.features.aircraft.components.AeroDexNavigationItem
import dev.afalabarce.aircrafts.presentation.ui.features.aircraft.components.AeroDexNavigationRail
import dev.afalabarce.aircrafts.presentation.ui.features.profile.screen.ProfilePane
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.Res
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.nav_back_exit_prompt
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.nav_compare
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.nav_compare_cd
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.nav_explore
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.nav_explore_cd
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.nav_favorites
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.nav_favorites_cd
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.nav_profile
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.nav_profile_cd
import dev.afalabarce.aircrafts.presentation.ui.navigation.AeroDexRoute
import dev.afalabarce.aircrafts.presentation.ui.navigation.aeroDexNavSavedStateConfig
import dev.afalabarce.aircrafts.presentation.ui.navigation.navigateToTab
import dev.afalabarce.aircrafts.presentation.ui.navigation.toTabOrNull
import dev.afalabarce.aircrafts.presentation.viewmodels.features.aircraft.AeroDexTab
import dev.afalabarce.aircrafts.presentation.viewmodels.features.aircraft.AircraftUiState
import dev.afalabarce.aircrafts.presentation.viewmodels.features.aircraft.AircraftViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private object AppLayoutTokens {
    const val DIVIDER_ALPHA = 0.5f
    const val EXPANDED_MASTER_WEIGHT = 0.43f
    const val MEDIUM_MASTER_WEIGHT = 0.46f
    const val FULL_WEIGHT = 1f
    const val INITIAL_PX = 0f
}

private object AppNavigationTokens {
    const val EXIT_CONFIRMATION_TIMEOUT_MS = 2000L
}

private fun handleBackNavigation(
    backStack: NavBackStack<NavKey>,
    viewModel: AircraftViewModel
) {
    if (backStack.size <= 1) return
    val removed = backStack.removeLastOrNull()
    if (removed is AeroDexRoute.AircraftDetail) {
        viewModel.clearSelection()
    }
    val currentTab = (backStack.lastOrNull() as? AeroDexRoute)?.toTabOrNull()
    if (currentTab != null) {
        viewModel.selectTab(currentTab)
    }
}

@Preview
@Composable
fun App() {
    val viewModel: AircraftViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val backStack = rememberNavBackStack(aeroDexNavSavedStateConfig, AeroDexRoute.Explore)

    val navExploreLabel = stringResource(Res.string.nav_explore)
    val navExploreCd = stringResource(Res.string.nav_explore_cd)
    val navFavoritesLabel = stringResource(Res.string.nav_favorites)
    val navFavoritesCd = stringResource(Res.string.nav_favorites_cd)
    val navCompareLabel = stringResource(Res.string.nav_compare)
    val navCompareCd = stringResource(Res.string.nav_compare_cd)
    val navProfileLabel = stringResource(Res.string.nav_profile)
    val navProfileCd = stringResource(Res.string.nav_profile_cd)

    val favoritesCount = uiState.favorites.size
    val compareCount = uiState.compareCodes.size
    val navItems = remember(
        favoritesCount,
        compareCount,
        navExploreLabel,
        navExploreCd,
        navFavoritesLabel,
        navFavoritesCd,
        navCompareLabel,
        navCompareCd,
        navProfileLabel,
        navProfileCd
    ) {
        listOf(
            AeroDexNavigationItem(
                label = navExploreLabel,
                contentDescription = navExploreCd,
                icon = { Icon(imageVector = Icons.Rounded.Flight, contentDescription = null) }
            ),
            AeroDexNavigationItem(
                label = navFavoritesLabel,
                contentDescription = navFavoritesCd,
                badgeCount = favoritesCount,
                icon = { Icon(imageVector = Icons.Rounded.Favorite, contentDescription = null) }
            ),
            AeroDexNavigationItem(
                label = navCompareLabel,
                contentDescription = navCompareCd,
                badgeCount = compareCount,
                icon = { Icon(imageVector = Icons.AutoMirrored.Rounded.CompareArrows, contentDescription = null) }
            ),
            AeroDexNavigationItem(
                label = navProfileLabel,
                contentDescription = navProfileCd,
                icon = { Icon(imageVector = Icons.Rounded.Person, contentDescription = null) }
            )
        )
    }

    val currentRoute = backStack.lastOrNull() as? AeroDexRoute
    val selectedTabIndex = currentRoute?.toTabOrNull()?.ordinal ?: uiState.selectedTab.ordinal
    val hingeBounds = rememberHingeBounds()
    val hasVerticalHinge = hingeBounds?.orientation == HingeOrientation.VERTICAL
    val selectTab = remember(viewModel, backStack) {
        { tab: AeroDexTab ->
            viewModel.selectTab(tab)
            navigateToTab(
                backStack = backStack,
                tab = tab
            )
        }
    }
    val onSelectTabIndex = remember(selectTab) {
        { index: Int -> selectTab(AeroDexTab.entries[index]) }
    }
    var isNavBarCompact by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val exitPromptMessage = stringResource(Res.string.nav_back_exit_prompt)
    var isExitConfirmationPending by remember { mutableStateOf(false) }

    LaunchedEffect(currentRoute, uiState.selectedIcaoCode) {
        isNavBarCompact = false
    }

    LaunchedEffect(isExitConfirmationPending) {
        if (isExitConfirmationPending) {
            delay(AppNavigationTokens.EXIT_CONFIRMATION_TIMEOUT_MS)
            isExitConfirmationPending = false
        }
    }

    LaunchedEffect(backStack.size, currentRoute) {
        if (backStack.size > 1 || currentRoute != AeroDexRoute.Explore) {
            isExitConfirmationPending = false
            snackbarHostState.currentSnackbarData?.dismiss()
        }
    }

    PlatformBackHandler(
        enabled = backStack.size <= 1 && !isExitConfirmationPending,
        onBack = {
            isExitConfirmationPending = true
            coroutineScope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(
                    message = exitPromptMessage,
                    duration = SnackbarDuration.Short
                )
            }
        }
    )

    AppMaterialTheme {
        val dimens = AppMaterialTheme.dimens
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val density = LocalDensity.current
            var containerLeftPx by remember { mutableFloatStateOf(AppLayoutTokens.INITIAL_PX) }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(
                            WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                        )
                    )
                    .then(
                        if (hasVerticalHinge) {
                            Modifier.onGloballyPositioned { coords ->
                                containerLeftPx = coords.positionInWindow().x
                            }
                        } else {
                            Modifier
                        }
                    )
            ) {
                val totalWidth = maxWidth
                val isTwoPane = totalWidth >= dimens.twoPaneBreakpoint ||
                    (hasVerticalHinge && hingeBounds.isSeparating)

                LaunchedEffect(isTwoPane) {
                    val selectedIcao = uiState.selectedIcaoCode
                    if (isTwoPane && backStack.lastOrNull() is AeroDexRoute.AircraftDetail) {
                        backStack.removeLastOrNull()
                    } else if (
                        !isTwoPane &&
                        selectedIcao != null &&
                        uiState.selectedTab != AeroDexTab.COMPARATOR &&
                        uiState.selectedTab != AeroDexTab.PROFILE &&
                        backStack.lastOrNull() !is AeroDexRoute.AircraftDetail
                    ) {
                        backStack.add(AeroDexRoute.AircraftDetail(selectedIcao))
                    }
                }

                val onAircraftClick = remember(viewModel, backStack, isTwoPane) {
                    { icaoCode: String ->
                        viewModel.selectAircraft(icaoCode)
                        if (!isTwoPane && backStack.lastOrNull() != AeroDexRoute.AircraftDetail(icaoCode)) {
                            backStack.add(AeroDexRoute.AircraftDetail(icaoCode))
                        }
                    }
                }

                if (isTwoPane) {
                    var railWidthDp by remember { mutableStateOf(dimens.navigationRailMinWidth) }
                    val containerLeftDp = with(density) { containerLeftPx.toDp() }
                    Row(modifier = Modifier.fillMaxSize()) {
                        AeroDexNavigationRail(
                            items = navItems,
                            selectedIndex = selectedTabIndex,
                            onSelected = onSelectTabIndex,
                            modifier = Modifier.onSizeChanged { size ->
                                railWidthDp = with(density) { size.width.toDp() }
                            }
                        )

                        VerticalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(
                                alpha = AppLayoutTokens.DIVIDER_ALPHA
                            )
                        )

                        NavDisplay(
                            backStack = backStack,
                            modifier = Modifier
                                .weight(AppLayoutTokens.FULL_WEIGHT)
                                .fillMaxHeight(),
                            onBack = {
                                handleBackNavigation(
                                    backStack = backStack,
                                    viewModel = viewModel
                                )
                            },
                            entryProvider = entryProvider {
                                entry<AeroDexRoute.Explore> {
                                    TwoPaneMasterDetailContent(
                                        showFavoritesMaster = false,
                                        uiState = uiState,
                                        viewModel = viewModel,
                                        totalWidth = totalWidth,
                                        railWidthDp = railWidthDp,
                                        containerLeftDp = containerLeftDp,
                                        hingeBounds = hingeBounds,
                                        onAircraftClick = onAircraftClick
                                    )
                                }
                                entry<AeroDexRoute.Favorites> {
                                    TwoPaneMasterDetailContent(
                                        showFavoritesMaster = true,
                                        uiState = uiState,
                                        viewModel = viewModel,
                                        totalWidth = totalWidth,
                                        railWidthDp = railWidthDp,
                                        containerLeftDp = containerLeftDp,
                                        hingeBounds = hingeBounds,
                                        onAircraftClick = onAircraftClick
                                    )
                                }
                                entry<AeroDexRoute.Comparator> {
                                    ComparatorPane(
                                        uiState = uiState,
                                        bottomContentPadding = dimens.none,
                                        onSwap = viewModel::swapCompare,
                                        onClear = viewModel::clearCompare,
                                        onExploreClick = { selectTab(AeroDexTab.EXPLORE) },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                entry<AeroDexRoute.Profile> {
                                    ProfilePane(
                                        bottomContentPadding = dimens.none,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                entry<AeroDexRoute.AircraftDetail> {
                                    TwoPaneMasterDetailContent(
                                        showFavoritesMaster = uiState.selectedTab == AeroDexTab.FAVORITES,
                                        uiState = uiState,
                                        viewModel = viewModel,
                                        totalWidth = totalWidth,
                                        railWidthDp = railWidthDp,
                                        containerLeftDp = containerLeftDp,
                                        hingeBounds = hingeBounds,
                                        onAircraftClick = onAircraftClick
                                    )
                                }
                            }
                        )
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
                        NavDisplay(
                            backStack = backStack,
                            modifier = Modifier.fillMaxSize(),
                            onBack = {
                                handleBackNavigation(
                                    backStack = backStack,
                                    viewModel = viewModel
                                )
                            },
                            entryProvider = entryProvider {
                                entry<AeroDexRoute.Explore> {
                                    AircraftSearchPane(
                                        uiState = uiState,
                                        onQueryChange = viewModel::onQueryChanged,
                                        onSearchSubmit = viewModel::submitSearch,
                                        onClearQuery = viewModel::clearQuery,
                                        onCategorySelected = viewModel::onCategorySelected,
                                        onAircraftClick = onAircraftClick,
                                        onLoadMore = viewModel::loadMore,
                                        onRetry = viewModel::retrySearch,
                                        bottomContentPadding = AeroDexFloatingBarClearance,
                                        onToggleFavorite = viewModel::toggleFavorite,
                                        onToggleCompare = viewModel::toggleCompare,
                                        onScrollDirectionChanged = { isScrollingDown ->
                                            isNavBarCompact = isScrollingDown
                                        },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                entry<AeroDexRoute.Favorites> {
                                    FavoritesPane(
                                        uiState = uiState,
                                        bottomContentPadding = AeroDexFloatingBarClearance,
                                        onAircraftClick = onAircraftClick,
                                        onToggleFavorite = viewModel::toggleFavorite,
                                        onToggleCompare = viewModel::toggleCompare,
                                        onScrollDirectionChanged = { isScrollingDown ->
                                            isNavBarCompact = isScrollingDown
                                        },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                entry<AeroDexRoute.Comparator> {
                                    ComparatorPane(
                                        uiState = uiState,
                                        bottomContentPadding = AeroDexFloatingBarClearance,
                                        onSwap = viewModel::swapCompare,
                                        onClear = viewModel::clearCompare,
                                        onExploreClick = { selectTab(AeroDexTab.EXPLORE) },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                entry<AeroDexRoute.Profile> {
                                    ProfilePane(
                                        bottomContentPadding = AeroDexFloatingBarClearance,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                entry<AeroDexRoute.AircraftDetail> { detailRoute ->
                                    LaunchedEffect(detailRoute.icaoTypeCode) {
                                        if (uiState.selectedIcaoCode != detailRoute.icaoTypeCode) {
                                            viewModel.selectAircraft(detailRoute.icaoTypeCode)
                                        }
                                    }
                                    AircraftDetailPane(
                                        uiState = uiState,
                                        showBackButton = true,
                                        onBack = {
                                            if (backStack.lastOrNull() is AeroDexRoute.AircraftDetail) {
                                                backStack.removeLastOrNull()
                                            }
                                            viewModel.clearSelection()
                                        },
                                        onRetry = viewModel::retryDetail,
                                        onToggleFavorite = viewModel::toggleFavoriteFromDetail,
                                        onToggleCompare = viewModel::toggleCompare,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        )

                        if (currentRoute !is AeroDexRoute.AircraftDetail) {
                            AeroDexNavigationBar(
                                items = navItems,
                                selectedIndex = selectedTabIndex,
                                isCompact = isNavBarCompact,
                                onSelected = onSelectTabIndex,
                                modifier = Modifier.align(Alignment.BottomCenter)
                            )
                        }
                    }
                }

                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(
                            bottom = if (!isTwoPane && currentRoute !is AeroDexRoute.AircraftDetail) {
                                AeroDexFloatingBarClearance
                            } else {
                                dimens.spacingMd
                            }
                        )
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                )
            }
        }
    }
}

@Composable
private fun TwoPaneMasterDetailContent(
    showFavoritesMaster: Boolean,
    uiState: AircraftUiState,
    viewModel: AircraftViewModel,
    totalWidth: Dp,
    railWidthDp: Dp,
    containerLeftDp: Dp,
    hingeBounds: HingeBounds?,
    onAircraftClick: (String) -> Unit
) {
    val dimens = AppMaterialTheme.dimens
    val dividerWidth = dimens.hairline
    val verticalHinge = hingeBounds?.takeIf {
        it.orientation == HingeOrientation.VERTICAL
    }
    val hingeLocalLeft = verticalHinge?.let {
        (it.left - containerLeftDp).coerceAtLeast(dimens.none)
    }
    val hingeMasterWidth = hingeLocalLeft?.let {
        (it - railWidthDp - dividerWidth).coerceAtLeast(dimens.minMasterPaneWidth)
    }
    val masterWeight = if (totalWidth >= dimens.expandedWidthBreakpoint) {
        AppLayoutTokens.EXPANDED_MASTER_WEIGHT
    } else {
        AppLayoutTokens.MEDIUM_MASTER_WEIGHT
    }

    Row(modifier = Modifier.fillMaxSize()) {
        val masterModifier = if (hingeMasterWidth != null) {
            Modifier.width(hingeMasterWidth).fillMaxHeight()
        } else {
            Modifier.weight(masterWeight).fillMaxHeight()
        }
        val detailModifier = if (hingeMasterWidth != null) {
            Modifier.weight(AppLayoutTokens.FULL_WEIGHT).fillMaxHeight()
        } else {
            Modifier.weight(AppLayoutTokens.FULL_WEIGHT - masterWeight).fillMaxHeight()
        }

        if (showFavoritesMaster) {
            FavoritesPane(
                uiState = uiState,
                bottomContentPadding = dimens.none,
                onAircraftClick = onAircraftClick,
                onToggleFavorite = viewModel::toggleFavorite,
                onToggleCompare = viewModel::toggleCompare,
                modifier = masterModifier
            )
        } else {
            AircraftSearchPane(
                uiState = uiState,
                onQueryChange = viewModel::onQueryChanged,
                onSearchSubmit = viewModel::submitSearch,
                onClearQuery = viewModel::clearQuery,
                onCategorySelected = viewModel::onCategorySelected,
                onAircraftClick = onAircraftClick,
                onLoadMore = viewModel::loadMore,
                onRetry = viewModel::retrySearch,
                onToggleFavorite = viewModel::toggleFavorite,
                onToggleCompare = viewModel::toggleCompare,
                modifier = masterModifier
            )
        }

        val hingeGap = verticalHinge?.width?.coerceAtLeast(dividerWidth) ?: dividerWidth
        Box(
            modifier = Modifier.width(hingeGap).fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            VerticalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(
                    alpha = AppLayoutTokens.DIVIDER_ALPHA
                )
            )
        }

        Box(modifier = detailModifier) {
            if (uiState.selectedIcaoCode != null) {
                AircraftDetailPane(
                    uiState = uiState,
                    showBackButton = false,
                    onBack = viewModel::clearSelection,
                    onRetry = viewModel::retryDetail,
                    onToggleFavorite = viewModel::toggleFavoriteFromDetail,
                    onToggleCompare = viewModel::toggleCompare
                )
            } else {
                EmptyDetailPlaceholder()
            }
        }
    }
}
