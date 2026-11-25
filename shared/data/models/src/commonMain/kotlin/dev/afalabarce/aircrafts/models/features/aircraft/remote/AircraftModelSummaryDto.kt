package dev.afalabarce.aircrafts.models.features.aircraft.remote

import kotlinx.serialization.Serializable

@Serializable
data class AircraftModelSummaryDto(
    val icaoTypeCode: String,
    val iataTypeCode: String? = null,
    val manufacturer: String? = null,
    val model: String? = null,
    val name: String,
    val category: String,
    val photo: PhotoDto? = null,
    val firstFlightYear: Int? = null,
    val engineCount: Int? = null,
    val engineType: String? = null,
    val wakeTurbulenceCategory: String? = null,
    val maxPassengers: Int? = null,
    val cruiseSpeedKt: Int? = null,
    val maxSpeedKt: Int? = null,
    val rangeNm: Int? = null,
    val rangeKm: Int? = null,
)
