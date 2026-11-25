package dev.afalabarce.aircrafts.models.features.aircraft.remote

import kotlinx.serialization.Serializable

@Serializable
data class PerformanceDto(
    val cruiseSpeedKt: Int? = null,
    val cruiseMach: Double? = null,
    val cruiseAltitudeFt: Int? = null,
    val maxSpeedKt: Int? = null,
    val maxSpeedKmh: Int? = null,
    val maxSpeedMach: Double? = null,
    val vmoKt: Int? = null,
    val mmo: Double? = null,
    val approachSpeedKt: Int? = null,
    val serviceCeilingFt: Int? = null,
    val absoluteCeilingFt: Int? = null,
    val rangeNm: Int? = null,
    val rangeKm: Int? = null,
    val fuelCapacityL: Int? = null,
)
