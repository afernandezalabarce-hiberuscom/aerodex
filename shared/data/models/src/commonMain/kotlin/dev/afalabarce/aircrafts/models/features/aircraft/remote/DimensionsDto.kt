package dev.afalabarce.aircrafts.models.features.aircraft.remote

import kotlinx.serialization.Serializable

@Serializable
data class DimensionsDto(
    val lengthM: Double? = null,
    val heightM: Double? = null,
    val wingspanM: Double? = null,
    val wingAreaM2: Double? = null,
    val wingSweepDeg: Double? = null,
    val wingMacM: Double? = null,
    val fuselageWidthM: Double? = null,
    val fuselageHeightM: Double? = null,
    val mainRotorDiameterM: Double? = null,
    val aerodromeReferenceCode: String? = null,
)
