package dev.afalabarce.aircrafts.models.features.aircraft.remote

import kotlinx.serialization.Serializable

@Serializable
data class PhotoDto(
    val url: String,
    val sourceUrl: String? = null,
    val author: String? = null,
    val authorProfileUrl: String? = null,
    val license: String? = null,
)
