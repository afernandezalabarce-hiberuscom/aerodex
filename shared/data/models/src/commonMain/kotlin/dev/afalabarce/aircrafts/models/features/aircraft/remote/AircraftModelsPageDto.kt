package dev.afalabarce.aircrafts.models.features.aircraft.remote

import kotlinx.serialization.Serializable

@Serializable
data class AircraftModelsPageDto(
    val items: List<AircraftModelSummaryDto> = emptyList(),
    val nextCursor: String? = null,
    val hasMore: Boolean = false,
    val limit: Int = 20,
)
