package dev.afalabarce.aircrafts.data.datasources.core.remote

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import dev.afalabarce.aircrafts.models.features.aircraft.remote.AircraftModelDetailDto
import dev.afalabarce.aircrafts.models.features.aircraft.remote.AircraftModelsPageDto

interface ApiService {
    @GET("api/v1/models")
    suspend fun listAircraftModels(
        @Query("limit") limit: Int = 20,
        @Query("cursor") cursor: String? = null,
        @Query("category") category: String? = null,
        @Query("q") query: String? = null
    ): AircraftModelsPageDto

    @GET("api/v1/models/{icaoCode}")
    suspend fun getAircraftModel(
        @Path("icaoCode") icaoCode: String
    ): AircraftModelDetailDto

    companion object {
        const val API_URL = "https://flightapi.afalabarce.dev/"
    }
}
