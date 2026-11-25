package dev.afalabarce.aircrafts.domain.repository.features.aircraft

import dev.afalabarce.aircrafts.domain.models.AircraftCategory
import dev.afalabarce.aircrafts.domain.models.AircraftDetailResult
import dev.afalabarce.aircrafts.domain.models.AircraftModelSummary
import dev.afalabarce.aircrafts.domain.models.AircraftSearchResult
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for searching aircraft models, retrieving technical sheets,
 * and managing local favorites.
 */
interface AircraftRepository {
    suspend fun searchAircraftModels(
        query: String,
        category: AircraftCategory? = null,
        cursor: String? = null,
        limit: Int = 20
    ): AircraftSearchResult

    suspend fun getAircraftModelDetail(icaoTypeCode: String): AircraftDetailResult

    fun observeFavoriteCodes(): Flow<Set<String>>

    fun observeFavorites(): Flow<List<AircraftModelSummary>>

    suspend fun toggleFavorite(aircraft: AircraftModelSummary): Boolean
}
