package dev.afalabarce.aircrafts.data.datasources.features.aircraft

import dev.afalabarce.aircrafts.models.features.aircraft.local.CachedAircraftSummaryEntity
import dev.afalabarce.aircrafts.models.features.aircraft.local.FavoriteAircraftEntity
import dev.afalabarce.aircrafts.models.features.aircraft.remote.AircraftModelDetailDto
import kotlinx.coroutines.flow.Flow

/**
 * Local persistent data source (Room) used for offline fallback and local favorites.
 */
interface AircraftLocalDataSource {
    suspend fun saveSummaries(summaries: List<CachedAircraftSummaryEntity>)

    suspend fun searchCachedSummaries(
        query: String,
        category: String?,
        limit: Int
    ): List<CachedAircraftSummaryEntity>

    suspend fun saveDetail(detail: AircraftModelDetailDto, updatedAtMs: Long)

    suspend fun getCachedDetail(icaoTypeCode: String): AircraftModelDetailDto?

    fun observeFavoriteCodes(): Flow<Set<String>>

    fun observeFavorites(): Flow<List<FavoriteAircraftEntity>>

    suspend fun toggleFavorite(entity: FavoriteAircraftEntity): Boolean
}
