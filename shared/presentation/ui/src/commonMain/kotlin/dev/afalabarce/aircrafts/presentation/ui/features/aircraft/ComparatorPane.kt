package dev.afalabarce.aircrafts.presentation.ui.features.aircraft

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.CompareArrows
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import coil3.compose.AsyncImage
import dev.afalabarce.aircrafts.core.ui.theme.AppMaterialTheme
import dev.afalabarce.aircrafts.core.ui.theme.hero_icon_tint
import dev.afalabarce.aircrafts.domain.models.AircraftModelDetail
import dev.afalabarce.aircrafts.presentation.ui.features.aircraft.components.AeroDexTopBar
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.Res
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.comparator_clear_cd
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.comparator_empty_subtitle
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.comparator_empty_title
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.comparator_fleet_title
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.comparator_go_catalog
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.comparator_length_title
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.comparator_max_capacity_title
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.comparator_mtow_title
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.comparator_slot_1
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.comparator_slot_2
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.comparator_speed_title
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.comparator_swap_cd
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.comparator_title
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.comparator_wingspan_title
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.metric_dash
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.metric_propulsion_title
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.metric_range_title
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.metric_unknown
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.nav_compare_cd
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.unit_kg
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.unit_km
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.unit_kt
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.unit_m
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.unit_mach_short
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.unit_pax
import dev.afalabarce.aircrafts.presentation.viewmodels.features.aircraft.AircraftUiState
import org.jetbrains.compose.resources.stringResource

private object ComparatorPaneTokens {
    const val COMPARE_PAIR_COUNT = 2
    const val HEADER_CARD_ALPHA = 0.4f
    const val METRIC_ROW_ALPHA = 0.3f
    const val HERO_ICON_ALPHA = 0.75f
    const val SINGLE_LINE = 1
    const val PLACEHOLDER_MAX_LINES = 2
    const val LEFT_INDEX = 0
    const val RIGHT_INDEX = 1
    const val MIN_POSITIVE_COUNT = 0
    const val ENGINE_COUNT_SUFFIX = "x "
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ComparatorPane(
    uiState: AircraftUiState,
    bottomContentPadding: Dp,
    onSwap: () -> Unit,
    onClear: () -> Unit,
    onExploreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimens = AppMaterialTheme.dimens

    Column(modifier = modifier.fillMaxSize()) {
        AeroDexTopBar(
            origin = null,
            subtitle = stringResource(Res.string.nav_compare_cd)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimens.appMargin, vertical = dimens.spacingXs),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.comparator_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() }
            )

            Row(horizontalArrangement = Arrangement.spacedBy(dimens.spacingXs)) {
                if (uiState.compareCodes.size == ComparatorPaneTokens.COMPARE_PAIR_COUNT) {
                    IconButton(onClick = onSwap) {
                        Icon(
                            imageVector = Icons.Rounded.SwapHoriz,
                            contentDescription = stringResource(Res.string.comparator_swap_cd),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                if (uiState.compareCodes.isNotEmpty()) {
                    IconButton(onClick = onClear) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = stringResource(Res.string.comparator_clear_cd),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        when {
            uiState.compareCodes.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(dimens.mediumMarginStartSurface)
                        .padding(bottom = bottomContentPadding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.CompareArrows,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(dimens.icon3Xl)
                    )
                    Spacer(modifier = Modifier.height(dimens.spacingLg))
                    Text(
                        text = stringResource(Res.string.comparator_empty_title),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(dimens.spacingSm))
                    Text(
                        text = stringResource(Res.string.comparator_empty_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(dimens.topSurface))
                    OutlinedButton(onClick = onExploreClick) {
                        Text(stringResource(Res.string.comparator_go_catalog))
                    }
                }
            }

            uiState.isCompareLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            }

            else -> {
                val left = uiState.compareLeftDetail
                val right = uiState.compareRightDetail
                val dash = stringResource(Res.string.metric_dash)
                val unknown = stringResource(Res.string.metric_unknown)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = dimens.appMargin)
                        .padding(
                            top = dimens.minTopSurface,
                            bottom = dimens.bottomSurface + bottomContentPadding
                        ),
                    verticalArrangement = Arrangement.spacedBy(dimens.spacingLg)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(dimens.spacingMd)
                    ) {
                        CompareHeaderCard(
                            detail = left,
                            placeholder = stringResource(Res.string.comparator_slot_1),
                            modifier = Modifier.weight(1f)
                        )
                        CompareHeaderCard(
                            detail = right,
                            placeholder = stringResource(Res.string.comparator_slot_2),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    CompareMetricRow(
                        title = stringResource(Res.string.metric_range_title),
                        leftValue = left?.performance?.rangeKm?.let {
                            stringResource(Res.string.unit_km, it)
                        } ?: dash,
                        rightValue = right?.performance?.rangeKm?.let {
                            stringResource(Res.string.unit_km, it)
                        } ?: dash
                    )
                    CompareMetricRow(
                        title = stringResource(Res.string.comparator_max_capacity_title),
                        leftValue = left?.capacity?.maxPassengers?.let {
                            stringResource(Res.string.unit_pax, it)
                        } ?: dash,
                        rightValue = right?.capacity?.maxPassengers?.let {
                            stringResource(Res.string.unit_pax, it)
                        } ?: dash
                    )
                    CompareMetricRow(
                        title = stringResource(Res.string.comparator_speed_title),
                        leftValue = left?.performance?.cruiseMach?.let {
                            stringResource(Res.string.unit_mach_short, it)
                        } ?: left?.performance?.vmoKt?.let {
                            stringResource(Res.string.unit_kt, it)
                        } ?: dash,
                        rightValue = right?.performance?.cruiseMach?.let {
                            stringResource(Res.string.unit_mach_short, it)
                        } ?: right?.performance?.vmoKt?.let {
                            stringResource(Res.string.unit_kt, it)
                        } ?: dash
                    )
                    CompareMetricRow(
                        title = stringResource(Res.string.metric_propulsion_title),
                        leftValue = left?.propulsion?.let {
                            "${it.engineCount ?: unknown}${ComparatorPaneTokens.ENGINE_COUNT_SUFFIX}${it.engineType ?: dash}"
                        } ?: dash,
                        rightValue = right?.propulsion?.let {
                            "${it.engineCount ?: unknown}${ComparatorPaneTokens.ENGINE_COUNT_SUFFIX}${it.engineType ?: dash}"
                        } ?: dash
                    )
                    CompareMetricRow(
                        title = stringResource(Res.string.comparator_wingspan_title),
                        leftValue = left?.dimensions?.wingspanM?.let {
                            stringResource(Res.string.unit_m, it)
                        } ?: dash,
                        rightValue = right?.dimensions?.wingspanM?.let {
                            stringResource(Res.string.unit_m, it)
                        } ?: dash
                    )
                    CompareMetricRow(
                        title = stringResource(Res.string.comparator_length_title),
                        leftValue = left?.dimensions?.lengthM?.let {
                            stringResource(Res.string.unit_m, it)
                        } ?: dash,
                        rightValue = right?.dimensions?.lengthM?.let {
                            stringResource(Res.string.unit_m, it)
                        } ?: dash
                    )
                    CompareMetricRow(
                        title = stringResource(Res.string.comparator_mtow_title),
                        leftValue = left?.weights?.mtowKg?.let {
                            stringResource(Res.string.unit_kg, it)
                        } ?: dash,
                        rightValue = right?.weights?.mtowKg?.let {
                            stringResource(Res.string.unit_kg, it)
                        } ?: dash
                    )
                    CompareMetricRow(
                        title = stringResource(Res.string.comparator_fleet_title),
                        leftValue = left?.fleetCount?.takeIf {
                            it > ComparatorPaneTokens.MIN_POSITIVE_COUNT
                        }?.toString() ?: dash,
                        rightValue = right?.fleetCount?.takeIf {
                            it > ComparatorPaneTokens.MIN_POSITIVE_COUNT
                        }?.toString() ?: dash
                    )
                }
            }
        }
    }
}

@Composable
private fun CompareHeaderCard(
    detail: AircraftModelDetail?,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    val dimens = AppMaterialTheme.dimens
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(dimens.cardCornerLg),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(
            alpha = ComparatorPaneTokens.HEADER_CARD_ALPHA
        )
    ) {
        Column(modifier = Modifier.padding(dimens.spacingMd)) {
            val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer
            val secondaryColor = MaterialTheme.colorScheme.secondary
            val heroGradient = remember(primaryContainerColor, secondaryColor) {
                Brush.linearGradient(colors = listOf(primaryContainerColor, secondaryColor))
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimens.cardImageCompactHeight)
                    .clip(RoundedCornerShape(dimens.textFieldCorner))
                    .background(heroGradient),
                contentAlignment = Alignment.Center
            ) {
                val url = detail?.photos?.firstOrNull()?.url
                if (url != null) {
                    AsyncImage(
                        model = url,
                        contentDescription = detail.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Flight,
                        contentDescription = null,
                        tint = hero_icon_tint.copy(alpha = ComparatorPaneTokens.HERO_ICON_ALPHA),
                        modifier = Modifier.size(dimens.smallIconSize)
                    )
                }
            }
            Spacer(modifier = Modifier.height(dimens.minTopSurface))
            if (detail != null) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = detail.icaoTypeCode,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(
                            horizontal = dimens.minStartSurface,
                            vertical = dimens.spacingXxs
                        )
                    )
                }
                Spacer(modifier = Modifier.height(dimens.spacingXs))
                Text(
                    text = detail.name,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = ComparatorPaneTokens.SINGLE_LINE,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = ComparatorPaneTokens.PLACEHOLDER_MAX_LINES
                )
            }
        }
    }
}

@Composable
private fun CompareMetricRow(
    title: String,
    leftValue: String,
    rightValue: String
) {
    val dimens = AppMaterialTheme.dimens
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimens.cardCornerMd),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(
            alpha = ComparatorPaneTokens.METRIC_ROW_ALPHA
        )
    ) {
        Column(
            modifier = Modifier.padding(dimens.spacingLg),
            verticalArrangement = Arrangement.spacedBy(dimens.minTopSurface)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimens.spacingMd)
            ) {
                Text(
                    text = leftValue,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = rightValue,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
