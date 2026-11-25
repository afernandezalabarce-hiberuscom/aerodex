package dev.afalabarce.aircrafts.models.features.aircraft.remote

import kotlinx.serialization.Serializable

@Serializable
data class WeightsDto(
    val mtowKg: Int? = null,
    val mlwKg: Int? = null,
    val mzfwKg: Int? = null,
    val mrwKg: Int? = null,
    val oewKg: Int? = null,
)
