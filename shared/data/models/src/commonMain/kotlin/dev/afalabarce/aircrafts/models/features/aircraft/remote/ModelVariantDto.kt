package dev.afalabarce.aircrafts.models.features.aircraft.remote

import kotlinx.serialization.Serializable

@Serializable
data class ModelVariantDto(
    val name: String,
    val engineManufacturer: String? = null,
    val engineModel: String? = null,
    val certifiedAt: String? = null,
)
