package dev.afalabarce.aircrafts.models.features.aircraft.remote

import kotlinx.serialization.Serializable

@Serializable
data class CapacityDto(
    val maxPassengers: Int? = null,
    val typicalPassengersLow: Int? = null,
    val typicalPassengersHigh: Int? = null,
)
