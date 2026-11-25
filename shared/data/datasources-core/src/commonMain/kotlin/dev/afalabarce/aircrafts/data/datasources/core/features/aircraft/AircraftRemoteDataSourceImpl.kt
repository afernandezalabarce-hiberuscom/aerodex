package dev.afalabarce.aircrafts.data.datasources.core.features.aircraft

import dev.afalabarce.aircrafts.data.datasources.core.remote.ApiService
import dev.afalabarce.aircrafts.data.datasources.features.aircraft.AircraftRemoteDataSource
import dev.afalabarce.aircrafts.models.features.aircraft.remote.AircraftModelDetailDto
import dev.afalabarce.aircrafts.models.features.aircraft.remote.AircraftModelsPageDto

class AircraftRemoteDataSourceImpl(
    private val apiService: ApiService
) : AircraftRemoteDataSource {
    override suspend fun searchModels(
        query: String?,
        category: String?,
        cursor: String?,
        limit: Int
    ): AircraftModelsPageDto {
        return apiService.listAircraftModels(
            limit = limit,
            cursor = cursor?.takeIf { it.isNotBlank() },
            category = category?.takeIf { it.isNotBlank() },
            query = query?.takeIf { it.isNotBlank() }
        )
    }

    override suspend fun getModelDetail(icaoTypeCode: String): AircraftModelDetailDto {
        return apiService.getAircraftModel(icaoTypeCode)
    }
}
