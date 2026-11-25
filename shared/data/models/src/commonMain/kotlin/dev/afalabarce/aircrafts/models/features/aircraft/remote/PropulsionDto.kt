package dev.afalabarce.aircrafts.models.features.aircraft.remote

import kotlinx.serialization.Serializable

@Serializable
data class PropulsionDto(
    val engineCount: Int? = null,
    val engineType: String? = null,
    val engineMount: String? = null,
    val defaultEngine: String? = null,
)
