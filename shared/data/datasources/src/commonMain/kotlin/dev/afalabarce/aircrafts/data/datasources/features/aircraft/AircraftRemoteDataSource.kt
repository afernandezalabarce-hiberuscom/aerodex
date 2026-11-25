package dev.afalabarce.aircrafts.data.datasources.features.aircraft

import dev.afalabarce.aircrafts.models.features.aircraft.remote.AircraftModelDetailDto
import dev.afalabarce.aircrafts.models.features.aircraft.remote.AircraftModelsPageDto

/**
 * Remote data source backed by FlightAPI (via Ktorfit).
 */
interface AircraftRemoteDataSource {
    suspend fun searchModels(
        query: String?,
        category: String?,
        cursor: String?,
        limit: Int
    ): AircraftModelsPageDto

    suspend fun getModelDetail(icaoTypeCode: String): AircraftModelDetailDto
}
