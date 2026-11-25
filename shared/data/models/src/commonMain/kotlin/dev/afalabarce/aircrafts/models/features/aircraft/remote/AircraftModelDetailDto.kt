package dev.afalabarce.aircrafts.models.features.aircraft.remote

import kotlinx.serialization.Serializable

@Serializable
data class AircraftModelDetailDto(
    val icaoTypeCode: String,
    val iataTypeCode: String? = null,
    val manufacturer: String? = null,
    val model: String? = null,
    val name: String,
    val category: String,
    val militaryRoles: List<String> = emptyList(),
    val countryOfOrigin: String? = null,
    val icaoDescription: String? = null,
    val aircraftClass: String? = null,
    val wakeTurbulenceCategory: String? = null,
    val firstFlightYear: Int? = null,
    val certificationYear: Int? = null,
    val typeCertificateUrl: String? = null,
    val wikipediaUrl: String? = null,
    val dimensions: DimensionsDto? = null,
    val weights: WeightsDto? = null,
    val performance: PerformanceDto? = null,
    val capacity: CapacityDto? = null,
    val propulsion: PropulsionDto? = null,
    val variants: List<ModelVariantDto> = emptyList(),
    val photos: List<PhotoDto> = emptyList(),
    val fleetCount: Int = 0,
    val dataSources: List<String> = emptyList(),
    val updatedAt: String? = null,
)
