package dev.afalabarce.aircrafts.presentation.ui.features.aircraft

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.CompareArrows
import androidx.compose.material.icons.rounded.AirlineSeatReclineNormal
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.FlightTakeoff
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material.icons.rounded.Scale
import androidx.compose.material.icons.rounded.SettingsInputComponent
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import coil3.compose.AsyncImage
import dev.afalabarce.aircrafts.core.ui.theme.AppMaterialTheme
import dev.afalabarce.aircrafts.core.ui.theme.hero_icon_tint
import dev.afalabarce.aircrafts.core.ui.theme.hero_pill_overlay
import dev.afalabarce.aircrafts.domain.models.AircraftModelDetail
import dev.afalabarce.aircrafts.domain.models.DataSourceOrigin
import dev.afalabarce.aircrafts.presentation.ui.features.aircraft.components.DataSourceBadge
import dev.afalabarce.aircrafts.presentation.ui.features.aircraft.components.OfflineBanner
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.Res
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.action_compare_add_cd
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.action_compare_remove_cd
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.action_favorite_add_cd
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.action_favorite_remove_cd
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.back
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.badge_cert_year
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.badge_iata
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.badge_icao
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.detail_ceiling_ft
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.detail_doc_8643
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.detail_empty_subtitle
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.detail_empty_title
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.detail_engine_mount
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.detail_error_title
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.detail_fleet_count
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.detail_height_m
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.detail_length_m
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.detail_list_separator
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.detail_max_passengers
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.detail_max_speed_kt
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.detail_mlw_kg
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.detail_mtow_kg
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.detail_oew_kg
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.detail_sources
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.detail_title_fallback
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.detail_typical_config
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.detail_variants_title
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.detail_vmo_kt
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.detail_wake_cat
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.detail_wing_area_m2
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.detail_wingspan_m
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.metric_capacity_title
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.metric_dimensions_title
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.metric_propulsion_title
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.metric_range_title
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.metric_speed_ceiling_title
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.metric_unavailable
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.metric_weights_title
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.retry
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.state_compared
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.state_favorite
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.state_not_compared
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.state_not_favorite
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.unit_kt
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.unit_km
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.unit_mach
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.unit_nm
import dev.afalabarce.aircrafts.presentation.viewmodels.features.aircraft.AircraftUiState
import org.jetbrains.compose.resources.stringResource

private object DetailPaneTokens {
    const val PLACEHOLDER_BG_ALPHA = 0.18f
    const val HERO_CARD_ALPHA = 0.35f
    const val HERO_ICON_ALPHA = 0.8f
    const val METRIC_CARD_ALPHA = 0.32f
    const val VARIANT_CHIP_ALPHA = 0.6f
    const val SINGLE_COL = 1
    const val TWO_COLS = 2
    const val THREE_COLS = 3
    const val MAX_VARIANTS_DISPLAYED = 10
    const val MIN_POSITIVE_COUNT = 0
    const val ENGINE_COUNT_SUFFIX = "x "
    const val SOURCES_SEPARATOR = ", "
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AircraftDetailPane(
    uiState: AircraftUiState,
    showBackButton: Boolean,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onToggleFavorite: (AircraftModelDetail) -> Unit = {},
    onToggleCompare: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val dimens = AppMaterialTheme.dimens

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimens.spacingLg, vertical = dimens.spacingMd),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.minStartSurface)
            ) {
                if (showBackButton) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(Res.string.back)
                        )
                    }
                }
                Text(
                    text = uiState.selectedDetail?.icaoTypeCode
                        ?: uiState.selectedIcaoCode
                        ?: stringResource(Res.string.detail_title_fallback),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.semantics { heading() }
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.spacingXs)
            ) {
                uiState.detailOrigin?.let { origin ->
                    DataSourceBadge(origin = origin)
                }
                val detail = uiState.selectedDetail
                if (detail != null) {
                    val isCompared = detail.icaoTypeCode.uppercase() in uiState.compareCodes
                    val isFavorite = detail.icaoTypeCode.uppercase() in uiState.favoriteCodes
                    val compareStateDesc = stringResource(
                        if (isCompared) Res.string.state_compared else Res.string.state_not_compared
                    )
                    val favoriteStateDesc = stringResource(
                        if (isFavorite) Res.string.state_favorite else Res.string.state_not_favorite
                    )
                    IconButton(
                        onClick = { onToggleCompare(detail.icaoTypeCode) },
                        modifier = Modifier.semantics { stateDescription = compareStateDesc }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.CompareArrows,
                            contentDescription = stringResource(
                                if (isCompared) {
                                    Res.string.action_compare_remove_cd
                                } else {
                                    Res.string.action_compare_add_cd
                                },
                                detail.name
                            ),
                            tint = if (isCompared) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                    IconButton(
                        onClick = { onToggleFavorite(detail) },
                        modifier = Modifier.semantics { stateDescription = favoriteStateDesc }
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
                                detail.name
                            ),
                            tint = if (isFavorite) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
        }

        when {
            uiState.isDetailLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            }

            uiState.detailError != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(dimens.mediumMarginStartSurface),
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
                        text = stringResource(Res.string.detail_error_title),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(dimens.spacingSm))
                    Text(
                        text = uiState.detailError?.toDisplayMessage().orEmpty(),
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

            uiState.selectedDetail != null -> {
                AircraftDetailContent(
                    detail = uiState.selectedDetail!!,
                    isOffline = uiState.detailOrigin == DataSourceOrigin.OFFLINE_CACHE,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun EmptyDetailPlaceholder(modifier: Modifier = Modifier) {
    val dimens = AppMaterialTheme.dimens
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(dimens.mediumMarginStartSurface),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(dimens.placeholderCircle)
                .clip(CircleShape)
                .background(
                    MaterialTheme.colorScheme.primaryContainer.copy(
                        alpha = DetailPaneTokens.PLACEHOLDER_BG_ALPHA
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.FlightTakeoff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(dimens.iconXl)
            )
        }
        Spacer(modifier = Modifier.height(dimens.topSurface))
        Text(
            text = stringResource(Res.string.detail_empty_title),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(dimens.minTopSurface))
        Text(
            text = stringResource(Res.string.detail_empty_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AircraftDetailContent(
    detail: AircraftModelDetail,
    isOffline: Boolean,
    modifier: Modifier = Modifier
) {
    val dimens = AppMaterialTheme.dimens
    val scrollState = rememberScrollState()
    val separator = stringResource(Res.string.detail_list_separator)
    val unavailable = stringResource(Res.string.metric_unavailable)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = dimens.spacing3Xl),
        verticalArrangement = Arrangement.spacedBy(dimens.appMargin)
    ) {
        if (isOffline) {
            OfflineBanner()
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimens.appMargin),
            shape = RoundedCornerShape(dimens.cardCorner2Xl),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = DetailPaneTokens.HERO_CARD_ALPHA
            )
        ) {
            Column(modifier = Modifier.padding(dimens.appMargin)) {
                val heroGradient = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.secondary
                    )
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimens.detailHeroHeight)
                        .clip(RoundedCornerShape(dimens.cardCornerLg))
                        .background(heroGradient)
                ) {
                    val photoUrl = detail.photos.firstOrNull()?.url
                    if (photoUrl != null) {
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = detail.name,
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
                                tint = hero_icon_tint.copy(alpha = DetailPaneTokens.HERO_ICON_ALPHA),
                                modifier = Modifier.size(dimens.iconHero)
                            )
                        }
                    }

                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimens.spacingLg),
                        horizontalArrangement = Arrangement.spacedBy(dimens.minStartSurface),
                        verticalArrangement = Arrangement.spacedBy(dimens.spacingSm)
                    ) {
                        HeroBadge(stringResource(Res.string.badge_icao, detail.icaoTypeCode))
                        detail.iataTypeCode?.let {
                            HeroBadge(stringResource(Res.string.badge_iata, it))
                        }
                        HeroBadge(detail.category.displayName)
                        detail.certificationYear?.let {
                            HeroBadge(stringResource(Res.string.badge_cert_year, it))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(dimens.spacingXl))

                detail.manufacturer?.let { manufacturer ->
                    Text(
                        text = manufacturer.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = detail.name,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                val subtitleParts = listOfNotNull(
                    detail.aircraftClass,
                    detail.icaoDescription?.let { stringResource(Res.string.detail_doc_8643, it) },
                    detail.wakeTurbulenceCategory?.let { stringResource(Res.string.detail_wake_cat, it) },
                    detail.fleetCount.takeIf { it > DetailPaneTokens.MIN_POSITIVE_COUNT }?.let {
                        stringResource(Res.string.detail_fleet_count, it)
                    }
                )
                if (subtitleParts.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(dimens.spacingXs))
                    Text(
                        text = subtitleParts.joinToString(separator),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimens.appMargin)
        ) {
            val cols = when {
                maxWidth >= dimens.threeColGridBreakpoint -> DetailPaneTokens.THREE_COLS
                maxWidth >= dimens.twoPaneCompactBreakpoint -> DetailPaneTokens.TWO_COLS
                else -> DetailPaneTokens.SINGLE_COL
            }

            val capacity = detail.capacity
            val cards = listOf(
                MetricCardData(
                    icon = Icons.Rounded.Route,
                    title = stringResource(Res.string.metric_range_title),
                    primaryValue = detail.performance?.rangeKm?.let {
                        stringResource(Res.string.unit_km, it)
                    } ?: unavailable,
                    secondaryValue = detail.performance?.rangeNm?.let {
                        stringResource(Res.string.unit_nm, it)
                    }
                ),
                MetricCardData(
                    icon = Icons.Rounded.AirlineSeatReclineNormal,
                    title = stringResource(Res.string.metric_capacity_title),
                    primaryValue = capacity?.maxPassengers?.let {
                        stringResource(Res.string.detail_max_passengers, it)
                    } ?: unavailable,
                    secondaryValue = if (
                        capacity?.typicalPassengersLow != null &&
                        capacity.typicalPassengersHigh != null
                    ) {
                        stringResource(
                            Res.string.detail_typical_config,
                            capacity.typicalPassengersLow!!,
                            capacity.typicalPassengersHigh!!
                        )
                    } else {
                        null
                    }
                ),
                MetricCardData(
                    icon = Icons.Rounded.Speed,
                    title = stringResource(Res.string.metric_speed_ceiling_title),
                    primaryValue = detail.performance?.cruiseMach?.let {
                        stringResource(Res.string.unit_mach, it)
                    } ?: detail.performance?.cruiseSpeedKt?.let {
                        stringResource(Res.string.unit_kt, it)
                    } ?: detail.performance?.maxSpeedKt?.let {
                        stringResource(Res.string.detail_max_speed_kt, it)
                    } ?: unavailable,
                    secondaryValue = listOfNotNull(
                        detail.performance?.vmoKt?.let {
                            stringResource(Res.string.detail_vmo_kt, it)
                        },
                        detail.performance?.serviceCeilingFt?.let {
                            stringResource(Res.string.detail_ceiling_ft, it)
                        }
                    ).joinToString(separator).ifEmpty { null }
                ),
                MetricCardData(
                    icon = Icons.Rounded.SettingsInputComponent,
                    title = stringResource(Res.string.metric_propulsion_title),
                    primaryValue = buildString {
                        detail.propulsion?.engineCount?.let {
                            append(it)
                            append(DetailPaneTokens.ENGINE_COUNT_SUFFIX)
                        }
                        append(detail.propulsion?.engineType ?: unavailable)
                    },
                    secondaryValue = listOfNotNull(
                        detail.propulsion?.defaultEngine,
                        detail.propulsion?.engineMount?.let {
                            stringResource(Res.string.detail_engine_mount, it)
                        }
                    ).joinToString(separator).ifEmpty { null }
                ),
                MetricCardData(
                    icon = Icons.Rounded.Straighten,
                    title = stringResource(Res.string.metric_dimensions_title),
                    primaryValue = listOfNotNull(
                        detail.dimensions?.wingspanM?.let {
                            stringResource(Res.string.detail_wingspan_m, it)
                        },
                        detail.dimensions?.lengthM?.let {
                            stringResource(Res.string.detail_length_m, it)
                        }
                    ).joinToString(separator).ifEmpty { unavailable },
                    secondaryValue = listOfNotNull(
                        detail.dimensions?.heightM?.let {
                            stringResource(Res.string.detail_height_m, it)
                        },
                        detail.dimensions?.wingAreaM2?.let {
                            stringResource(Res.string.detail_wing_area_m2, it)
                        }
                    ).joinToString(separator).ifEmpty { null }
                ),
                MetricCardData(
                    icon = Icons.Rounded.Scale,
                    title = stringResource(Res.string.metric_weights_title),
                    primaryValue = detail.weights?.mtowKg?.let {
                        stringResource(Res.string.detail_mtow_kg, it)
                    } ?: unavailable,
                    secondaryValue = listOfNotNull(
                        detail.weights?.mlwKg?.let {
                            stringResource(Res.string.detail_mlw_kg, it)
                        },
                        detail.weights?.oewKg?.let {
                            stringResource(Res.string.detail_oew_kg, it)
                        }
                    ).joinToString(separator).ifEmpty { null }
                )
            )

            Column(verticalArrangement = Arrangement.spacedBy(dimens.spacingLg)) {
                cards.chunked(cols).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(dimens.spacingLg)
                    ) {
                        rowItems.forEach { item ->
                            MetricCard(
                                data = item,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        repeat(cols - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        if (detail.variants.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimens.appMargin),
                verticalArrangement = Arrangement.spacedBy(dimens.minTopSurface)
            ) {
                Text(
                    text = stringResource(Res.string.detail_variants_title, detail.variants.size),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(dimens.minStartSurface),
                    verticalArrangement = Arrangement.spacedBy(dimens.minTopSurface)
                ) {
                    detail.variants.take(DetailPaneTokens.MAX_VARIANTS_DISPLAYED).forEach { variant ->
                        Surface(
                            shape = RoundedCornerShape(dimens.textFieldCorner),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(
                                alpha = DetailPaneTokens.VARIANT_CHIP_ALPHA
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(
                                    horizontal = dimens.spacingLg,
                                    vertical = dimens.minTopSurface
                                )
                            ) {
                                Text(
                                    text = variant.name,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                variant.engineModel?.let { engine ->
                                    Text(
                                        text = engine,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (detail.dataSources.isNotEmpty()) {
            Text(
                text = stringResource(
                    Res.string.detail_sources,
                    detail.dataSources.joinToString(DetailPaneTokens.SOURCES_SEPARATOR)
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = dimens.appMargin)
            )
        }
    }
}

@Composable
private fun HeroBadge(text: String) {
    val dimens = AppMaterialTheme.dimens
    Surface(
        shape = CircleShape,
        color = hero_pill_overlay
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = hero_icon_tint,
            modifier = Modifier.padding(
                horizontal = dimens.spacingMd,
                vertical = dimens.spacingXs
            )
        )
    }
}

private data class MetricCardData(
    val icon: ImageVector,
    val title: String,
    val primaryValue: String,
    val secondaryValue: String?
)

@Composable
private fun MetricCard(
    data: MetricCardData,
    modifier: Modifier = Modifier
) {
    val dimens = AppMaterialTheme.dimens
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(dimens.cardCornerLg),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(
            alpha = DetailPaneTokens.METRIC_CARD_ALPHA
        )
    ) {
        Column(
            modifier = Modifier.padding(dimens.spacingXl),
            verticalArrangement = Arrangement.spacedBy(dimens.spacingSm)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.minStartSurface)
            ) {
                Icon(
                    imageVector = data.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(dimens.iconSm)
                )
                Text(
                    text = data.title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = data.primaryValue,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (data.secondaryValue != null) {
                Text(
                    text = data.secondaryValue,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
