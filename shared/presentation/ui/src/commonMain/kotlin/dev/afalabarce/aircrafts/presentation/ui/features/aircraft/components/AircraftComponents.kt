package dev.afalabarce.aircrafts.presentation.ui.features.aircraft.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.CompareArrows
import androidx.compose.material.icons.rounded.AirlineSeatReclineNormal
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SettingsInputComponent
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import coil3.compose.AsyncImage
import dev.afalabarce.aircrafts.core.ui.theme.AppMaterialTheme
import dev.afalabarce.aircrafts.core.ui.theme.hero_icon_tint
import dev.afalabarce.aircrafts.core.ui.theme.hero_pill_overlay
import dev.afalabarce.aircrafts.domain.models.AircraftCategory
import dev.afalabarce.aircrafts.domain.models.AircraftModelSummary
import dev.afalabarce.aircrafts.domain.models.DataSourceOrigin
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.Res
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.action_compare_add_cd
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.action_compare_remove_cd
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.action_favorite_add_cd
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.action_favorite_remove_cd
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.app_name
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.app_subtitle
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.badge_icao
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.badge_live
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.badge_memory_cache
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.badge_offline
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.clear_search
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.filter_all_categories
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.filters_button_cd
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.filters_clear
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.filters_close
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.filters_title
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.metric_dash
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.offline_banner
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.search_icon_cd
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.search_placeholder
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.state_compared
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.state_favorite
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.state_not_compared
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.state_not_favorite
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.unit_km
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.unit_pax
import kotlinx.coroutines.flow.distinctUntilChanged
import org.jetbrains.compose.resources.stringResource

private object AircraftComponentsTokens {
    const val ALL_CATEGORY_KEY = "ALL"
    const val NETWORK_BADGE_ALPHA = 0.22f
    const val SEARCH_FOCUSED_ALPHA = 0.5f
    const val SEARCH_UNFOCUSED_ALPHA = 0.45f
    const val FILTER_CONTAINER_ALPHA = 0.7f
    const val CARD_BORDER_ALPHA = 0.45f
    const val CARD_SELECTED_ALPHA = 0.45f
    const val CARD_UNSELECTED_ALPHA = 0.35f
    const val HERO_GRADIENT_START_ALPHA = 0.8f
    const val HERO_GRADIENT_END_ALPHA = 0.7f
    const val HERO_ICON_ALPHA = 0.75f
    const val CATEGORY_PILL_ALPHA = 0.92f
    const val SINGLE_LINE = 1
    const val ALL_FILTER_INDEX = 0
    const val CATEGORY_INDEX_OFFSET = 1
    const val ENGINE_COUNT_SUFFIX = "x "
}

@Composable
fun AeroDexTopBar(
    origin: DataSourceOrigin?,
    modifier: Modifier = Modifier,
    subtitle: String = stringResource(Res.string.app_subtitle),
) {
    val dimens = AppMaterialTheme.dimens
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = dimens.appMargin, vertical = dimens.spacingLg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.spacingLg)
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.spacingLg)
        ) {
            Box(
                modifier = Modifier
                    .size(dimens.avatarContainer)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Flight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(dimens.iconLg)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.app_name).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    maxLines = AircraftComponentsTokens.SINGLE_LINE,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.semantics { heading() }
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = AircraftComponentsTokens.SINGLE_LINE,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (origin != null) {
            DataSourceBadge(origin = origin)
        }
    }
}

private data class BadgeStyle(
    val label: String,
    val icon: ImageVector,
    val containerColor: Color,
    val contentColor: Color,
)

@Composable
fun DataSourceBadge(
    origin: DataSourceOrigin,
    modifier: Modifier = Modifier
) {
    val dimens = AppMaterialTheme.dimens
    val style = when (origin) {
        DataSourceOrigin.NETWORK -> BadgeStyle(
            label = stringResource(Res.string.badge_live),
            icon = Icons.Rounded.CloudDone,
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                alpha = AircraftComponentsTokens.NETWORK_BADGE_ALPHA
            ),
            contentColor = MaterialTheme.colorScheme.primary
        )
        DataSourceOrigin.MEMORY_CACHE -> BadgeStyle(
            label = stringResource(Res.string.badge_memory_cache),
            icon = Icons.Rounded.Memory,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
        DataSourceOrigin.OFFLINE_CACHE -> BadgeStyle(
            label = stringResource(Res.string.badge_offline),
            icon = Icons.Rounded.CloudOff,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }

    Surface(
        modifier = modifier.semantics { liveRegion = LiveRegionMode.Polite },
        shape = CircleShape,
        color = style.containerColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = dimens.spacingMd, vertical = dimens.spacingSm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm)
        ) {
            Icon(
                imageVector = style.icon,
                contentDescription = null,
                tint = style.contentColor,
                modifier = Modifier.size(dimens.iconXs)
            )
            Text(
                text = style.label,
                style = MaterialTheme.typography.labelSmall,
                color = style.contentColor,
                maxLines = AircraftComponentsTokens.SINGLE_LINE,
                softWrap = false
            )
        }
    }
}

@Composable
fun AeroDexSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchSubmit: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textFieldState = rememberTextFieldState(initialText = query)

    LaunchedEffect(textFieldState) {
        snapshotFlow { textFieldState.text.toString() }
            .distinctUntilChanged()
            .collect { currentText ->
                onQueryChange(currentText)
            }
    }

    LaunchedEffect(query) {
        if (textFieldState.text.toString() != query) {
            textFieldState.setTextAndPlaceCursorAtEnd(query)
        }
    }

    OutlinedTextField(
        state = textFieldState,
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = stringResource(Res.string.search_placeholder),
                maxLines = AircraftComponentsTokens.SINGLE_LINE,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = stringResource(Res.string.search_icon_cd),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingIcon = {
            if (textFieldState.text.isNotEmpty()) {
                IconButton(
                    onClick = {
                        textFieldState.clearText()
                        onClear()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Clear,
                        contentDescription = stringResource(Res.string.clear_search),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        lineLimits = TextFieldLineLimits.SingleLine,
        shape = CircleShape,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        onKeyboardAction = { onSearchSubmit() },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = AircraftComponentsTokens.SEARCH_FOCUSED_ALPHA
            ),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = AircraftComponentsTokens.SEARCH_UNFOCUSED_ALPHA
            ),
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.Transparent
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CategoryFilterChips(
    selectedCategory: AircraftCategory?,
    onCategorySelected: (AircraftCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    val dimens = AppMaterialTheme.dimens
    var showFilterSheet by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val allLabel = stringResource(Res.string.filter_all_categories)

    LaunchedEffect(selectedCategory) {
        val targetIndex = if (selectedCategory == null) {
            AircraftComponentsTokens.ALL_FILTER_INDEX
        } else {
            AircraftCategory.entries.indexOf(selectedCategory) +
                AircraftComponentsTokens.CATEGORY_INDEX_OFFSET
        }
        if (targetIndex >= AircraftComponentsTokens.ALL_FILTER_INDEX) {
            listState.animateScrollToItem(index = targetIndex)
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = dimens.appMargin, end = dimens.spacingLg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.minStartSurface)
    ) {
        FilledTonalIconButton(
            onClick = { showFilterSheet = true },
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = if (selectedCategory != null) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(
                        alpha = AircraftComponentsTokens.FILTER_CONTAINER_ALPHA
                    )
                },
                contentColor = if (selectedCategory != null) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        ) {
            BadgedBox(
                badge = {
                    if (selectedCategory != null) {
                        Badge(containerColor = MaterialTheme.colorScheme.primary)
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Tune,
                    contentDescription = stringResource(Res.string.filters_button_cd),
                    modifier = Modifier.size(dimens.iconMd)
                )
            }
        }

        LazyRow(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(end = dimens.minEndSurface),
            horizontalArrangement = Arrangement.spacedBy(dimens.minStartSurface)
        ) {
            item(key = AircraftComponentsTokens.ALL_CATEGORY_KEY) {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { onCategorySelected(null) },
                    label = {
                        Text(
                            text = allLabel,
                            maxLines = AircraftComponentsTokens.SINGLE_LINE,
                            softWrap = false
                        )
                    },
                    shape = CircleShape,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
            items(AircraftCategory.entries, key = { it.name }) { category ->
                val selected = selectedCategory == category
                FilterChip(
                    selected = selected,
                    onClick = { onCategorySelected(category) },
                    label = {
                        Text(
                            text = category.displayName,
                            maxLines = AircraftComponentsTokens.SINGLE_LINE,
                            softWrap = false
                        )
                    },
                    shape = CircleShape,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
    }

    if (showFilterSheet) {
        val sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimens.spacing2Xl)
                    .padding(bottom = dimens.spacing4Xl),
                verticalArrangement = Arrangement.spacedBy(dimens.appMargin)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(Res.string.filters_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (selectedCategory != null) {
                        TextButton(
                            onClick = {
                                onCategorySelected(null)
                                showFilterSheet = false
                            }
                        ) {
                            Text(text = stringResource(Res.string.filters_clear))
                        }
                    } else {
                        TextButton(onClick = { showFilterSheet = false }) {
                            Text(text = stringResource(Res.string.filters_close))
                        }
                    }
                }

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dimens.minStartSurface),
                    verticalArrangement = Arrangement.spacedBy(dimens.minTopSurface)
                ) {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = {
                            onCategorySelected(null)
                            showFilterSheet = false
                        },
                        label = { Text(text = allLabel) },
                        shape = CircleShape,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                    AircraftCategory.entries.forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = {
                                onCategorySelected(category)
                                showFilterSheet = false
                            },
                            label = { Text(text = category.displayName) },
                            shape = CircleShape,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OfflineBanner(
    modifier: Modifier = Modifier
) {
    val dimens = AppMaterialTheme.dimens
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = dimens.appMargin)
            .semantics { liveRegion = LiveRegionMode.Polite },
        shape = AppMaterialTheme.shapes.medium,
        color = AppMaterialTheme.colorScheme.tertiaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = dimens.spacingXl, vertical = dimens.spacingMd),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.spacingMd)
        ) {
            Icon(
                imageVector = Icons.Rounded.CloudOff,
                contentDescription = null,
                tint = AppMaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(dimens.iconSm)
            )
            Text(
                text = stringResource(Res.string.offline_banner),
                style = MaterialTheme.typography.bodySmall,
                color = AppMaterialTheme.colorScheme.onTertiaryContainer,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AircraftCard(
    aircraft: AircraftModelSummary,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    isCompared: Boolean = false,
    onToggleFavorite: () -> Unit = {},
    onToggleCompare: () -> Unit = {}
) {
    val dimens = AppMaterialTheme.dimens
    val border = if (isSelected) {
        BorderStroke(dimens.thinBorder, MaterialTheme.colorScheme.primary)
    } else {
        BorderStroke(
            dimens.hairline,
            MaterialTheme.colorScheme.outlineVariant.copy(
                alpha = AircraftComponentsTokens.CARD_BORDER_ALPHA
            )
        )
    }

    val cardShape = RoundedCornerShape(dimens.cardCornerXl)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .clickable(onClick = onClick),
        shape = cardShape,
        color = if (isSelected) {
            MaterialTheme.colorScheme.secondaryContainer.copy(
                alpha = AircraftComponentsTokens.CARD_SELECTED_ALPHA
            )
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = AircraftComponentsTokens.CARD_UNSELECTED_ALPHA
            )
        },
        border = border
    ) {
        Column(modifier = Modifier.padding(dimens.spacingXl)) {
            val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer
            val secondaryColor = MaterialTheme.colorScheme.secondary
            val heroGradient = remember(primaryContainerColor, secondaryColor) {
                Brush.linearGradient(
                    colors = listOf(
                        primaryContainerColor.copy(
                            alpha = AircraftComponentsTokens.HERO_GRADIENT_START_ALPHA
                        ),
                        secondaryColor.copy(
                            alpha = AircraftComponentsTokens.HERO_GRADIENT_END_ALPHA
                        )
                    )
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimens.cardImageHeight)
                    .clip(RoundedCornerShape(dimens.dialogCorner))
                    .background(heroGradient)
            ) {
                val photoUrl = aircraft.photo?.url
                if (photoUrl != null) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = aircraft.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Flight,
                            contentDescription = null,
                            tint = hero_icon_tint.copy(alpha = AircraftComponentsTokens.HERO_ICON_ALPHA),
                            modifier = Modifier.size(dimens.icon2Xl)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimens.spacingMd),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = hero_pill_overlay
                    ) {
                        Text(
                            text = stringResource(Res.string.badge_icao, aircraft.icaoTypeCode),
                            style = MaterialTheme.typography.labelSmall,
                            color = hero_icon_tint,
                            maxLines = AircraftComponentsTokens.SINGLE_LINE,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(
                                horizontal = dimens.spacingMd,
                                vertical = dimens.spacingXs
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(dimens.minStartSurface))

                    Surface(
                        modifier = Modifier.weight(1f, fill = false),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface.copy(
                            alpha = AircraftComponentsTokens.CATEGORY_PILL_ALPHA
                        )
                    ) {
                        Text(
                            text = aircraft.category.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = AircraftComponentsTokens.SINGLE_LINE,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(
                                horizontal = dimens.spacingMd,
                                vertical = dimens.spacingXs
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(dimens.spacingLg))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    aircraft.manufacturer?.let { manufacturer ->
                        Text(
                            text = manufacturer.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = AircraftComponentsTokens.SINGLE_LINE,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = aircraft.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = AircraftComponentsTokens.SINGLE_LINE,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                val compareStateDesc = stringResource(
                    if (isCompared) Res.string.state_compared else Res.string.state_not_compared
                )
                val favoriteStateDesc = stringResource(
                    if (isFavorite) Res.string.state_favorite else Res.string.state_not_favorite
                )

                Row(horizontalArrangement = Arrangement.spacedBy(dimens.spacingXxs)) {
                    IconButton(
                        onClick = onToggleCompare,
                        modifier = Modifier
                            .defaultMinSize(
                                minWidth = dimens.minTouchTarget,
                                minHeight = dimens.minTouchTarget
                            )
                            .semantics { stateDescription = compareStateDesc }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.CompareArrows,
                            contentDescription = stringResource(
                                if (isCompared) {
                                    Res.string.action_compare_remove_cd
                                } else {
                                    Res.string.action_compare_add_cd
                                },
                                aircraft.name
                            ),
                            tint = if (isCompared) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(dimens.iconMd)
                        )
                    }
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .defaultMinSize(
                                minWidth = dimens.minTouchTarget,
                                minHeight = dimens.minTouchTarget
                            )
                            .semantics { stateDescription = favoriteStateDesc }
                    ) {
                        Icon(
                            imageVector = if (isFavorite) {
                                Icons.Rounded.Favorite
                            } else {
                                Icons.Rounded.FavoriteBorder
                            },
                            contentDescription = stringResource(
                                if (isFavorite) {
                                    Res.string.action_favorite_remove_cd
                                } else {
                                    Res.string.action_favorite_add_cd
                                },
                                aircraft.name
                            ),
                            tint = if (isFavorite) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(dimens.iconMd)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(dimens.spacingMd))

            val dash = stringResource(Res.string.metric_dash)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
                verticalArrangement = Arrangement.spacedBy(dimens.spacingSm)
            ) {
                SpecMiniPill(
                    icon = Icons.Rounded.AirlineSeatReclineNormal,
                    text = aircraft.maxPassengers?.let { stringResource(Res.string.unit_pax, it) } ?: dash
                )
                SpecMiniPill(
                    icon = Icons.Rounded.Route,
                    text = aircraft.rangeKm?.let { stringResource(Res.string.unit_km, it) } ?: dash
                )
                SpecMiniPill(
                    icon = Icons.Rounded.SettingsInputComponent,
                    text = buildString {
                        aircraft.engineCount?.let {
                            append(it)
                            append(AircraftComponentsTokens.ENGINE_COUNT_SUFFIX)
                        }
                        append(aircraft.engineType ?: dash)
                    }
                )
            }
        }
    }
}

@Composable
private fun SpecMiniPill(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    val dimens = AppMaterialTheme.dimens
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(dimens.spacingMd),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = dimens.minStartSurface, vertical = dimens.spacingSm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.spacingXs)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(dimens.iconXs)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = AircraftComponentsTokens.SINGLE_LINE,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
