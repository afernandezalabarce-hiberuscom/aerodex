package dev.afalabarce.aircrafts.domain.models

/**
 * Aircraft category supported by the FlightAPI catalog.
 */
enum class AircraftCategory(val apiValue: String, val displayName: String) {
    COMMERCIAL("COMMERCIAL", "Comercial"),
    MILITARY("MILITARY", "Militar"),
    BUSINESS("BUSINESS", "Ejecutivo"),
    GENERAL_AVIATION("GENERAL_AVIATION", "Aviación General"),
    ROTORCRAFT("ROTORCRAFT", "Helicópteros");

    companion object {
        fun fromApiValue(value: String?): AircraftCategory =
            entries.firstOrNull { it.apiValue.equals(value, ignoreCase = true) } ?: COMMERCIAL
    }
}

/**
 * Indicates where a repository result was served from.
 */
enum class DataSourceOrigin {
    NETWORK,
    MEMORY_CACHE,
    OFFLINE_CACHE
}

/**
 * Paginated search result returned by [dev.afalabarce.aircrafts.domain.repository.features.aircraft.AircraftRepository].
 */
data class AircraftSearchResult(
    val items: List<AircraftModelSummary>,
    val nextCursor: String?,
    val hasMore: Boolean,
    val origin: DataSourceOrigin
)

/**
 * Detail result returned by [dev.afalabarce.aircrafts.domain.repository.features.aircraft.AircraftRepository].
 */
data class AircraftDetailResult(
    val detail: AircraftModelDetail,
    val origin: DataSourceOrigin
)

data class AircraftPhoto(
    val url: String,
    val sourceUrl: String? = null,
    val author: String? = null,
    val license: String? = null
)

data class AircraftModelSummary(
    val icaoTypeCode: String,
    val iataTypeCode: String? = null,
    val manufacturer: String? = null,
    val model: String? = null,
    val name: String,
    val category: AircraftCategory,
    val photo: AircraftPhoto? = null,
    val firstFlightYear: Int? = null,
    val engineCount: Int? = null,
    val engineType: String? = null,
    val wakeTurbulenceCategory: String? = null,
    val maxPassengers: Int? = null,
    val cruiseSpeedKt: Int? = null,
    val maxSpeedKt: Int? = null,
    val rangeNm: Int? = null,
    val rangeKm: Int? = null
)

data class AircraftDimensions(
    val lengthM: Double? = null,
    val heightM: Double? = null,
    val wingspanM: Double? = null,
    val wingAreaM2: Double? = null,
    val wingSweepDeg: Double? = null,
    val fuselageWidthM: Double? = null,
    val mainRotorDiameterM: Double? = null,
    val aerodromeReferenceCode: String? = null
)

data class AircraftWeights(
    val mtowKg: Int? = null,
    val mlwKg: Int? = null,
    val mzfwKg: Int? = null,
    val mrwKg: Int? = null,
    val oewKg: Int? = null
)

data class AircraftPerformance(
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
    val rangeNm: Int? = null,
    val rangeKm: Int? = null,
    val fuelCapacityL: Int? = null
)

data class AircraftCapacity(
    val maxPassengers: Int? = null,
    val typicalPassengersLow: Int? = null,
    val typicalPassengersHigh: Int? = null
)

data class AircraftPropulsion(
    val engineCount: Int? = null,
    val engineType: String? = null,
    val engineMount: String? = null,
    val defaultEngine: String? = null
)

data class AircraftVariant(
    val name: String,
    val engineManufacturer: String? = null,
    val engineModel: String? = null,
    val certifiedAt: String? = null
)

data class AircraftModelDetail(
    val icaoTypeCode: String,
    val iataTypeCode: String? = null,
    val manufacturer: String? = null,
    val model: String? = null,
    val name: String,
    val category: AircraftCategory,
    val militaryRoles: List<String> = emptyList(),
    val countryOfOrigin: String? = null,
    val icaoDescription: String? = null,
    val aircraftClass: String? = null,
    val wakeTurbulenceCategory: String? = null,
    val firstFlightYear: Int? = null,
    val certificationYear: Int? = null,
    val typeCertificateUrl: String? = null,
    val wikipediaUrl: String? = null,
    val dimensions: AircraftDimensions? = null,
    val weights: AircraftWeights? = null,
    val performance: AircraftPerformance? = null,
    val capacity: AircraftCapacity? = null,
    val propulsion: AircraftPropulsion? = null,
    val variants: List<AircraftVariant> = emptyList(),
    val photos: List<AircraftPhoto> = emptyList(),
    val fleetCount: Int = 0,
    val dataSources: List<String> = emptyList()
)
