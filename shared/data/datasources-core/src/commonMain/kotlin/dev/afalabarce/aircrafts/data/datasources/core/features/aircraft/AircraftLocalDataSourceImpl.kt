package dev.afalabarce.aircrafts.data.datasources.core.features.aircraft

import dev.afalabarce.aircrafts.data.datasources.features.aircraft.AircraftLocalDataSource
import dev.afalabarce.aircrafts.models.features.aircraft.local.CachedAircraftDetailEntity
import dev.afalabarce.aircrafts.models.features.aircraft.local.CachedAircraftSummaryEntity
import dev.afalabarce.aircrafts.models.features.aircraft.local.FavoriteAircraftEntity
import dev.afalabarce.aircrafts.models.features.aircraft.remote.AircraftModelDetailDto
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * Local Room + JSON persistence implementation for aircraft summaries, details, and favorites.
 *
 * Threading & Mutual Exclusion decisions:
 * - [ioDispatcher] (defaults to [Dispatchers.IO]) offloads JSON serialization/deserialization and
 *   DAO coordination off the caller's dispatcher (e.g. `Dispatchers.Main`).
 * - [favoriteMutex] serializes the check-then-act sequence in [toggleFavorite] so concurrent
 *   taps on the same aircraft never interleave `isFavorite` and `insertFavorite`/`deleteFavorite`.
 */
class AircraftLocalDataSourceImpl(
    private val aircraftDao: AircraftDao,
    private val json: Json,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AircraftLocalDataSource {
    private val favoriteMutex = Mutex()

    override suspend fun saveSummaries(summaries: List<CachedAircraftSummaryEntity>) {
        if (summaries.isEmpty()) return
        withContext(ioDispatcher) {
            aircraftDao.upsertSummaries(summaries)
        }
    }

    override suspend fun searchCachedSummaries(
        query: String,
        category: String?,
        limit: Int
    ): List<CachedAircraftSummaryEntity> = withContext(ioDispatcher) {
        aircraftDao.searchSummaries(
            query = query.trim(),
            category = category,
            limit = limit
        )
    }

    override suspend fun saveDetail(detail: AircraftModelDetailDto, updatedAtMs: Long) {
        withContext(ioDispatcher) {
            val payload = json.encodeToString(AircraftModelDetailDto.serializer(), detail)
            aircraftDao.upsertDetail(
                CachedAircraftDetailEntity(
                    icaoTypeCode = detail.icaoTypeCode.uppercase(),
                    payloadJson = payload,
                    updatedAtMs = updatedAtMs
                )
            )
        }
    }

    override suspend fun getCachedDetail(icaoTypeCode: String): AircraftModelDetailDto? =
        withContext(ioDispatcher) {
            val cached = aircraftDao.getDetail(icaoTypeCode.uppercase()) ?: return@withContext null
            runCatching {
                json.decodeFromString(AircraftModelDetailDto.serializer(), cached.payloadJson)
            }.getOrNull()
        }

    override fun observeFavoriteCodes(): Flow<Set<String>> =
        aircraftDao.observeFavoriteCodes().map { codes -> codes.mapTo(HashSet(codes.size)) { it.uppercase() } }

    override fun observeFavorites(): Flow<List<FavoriteAircraftEntity>> =
        aircraftDao.observeFavorites()

    override suspend fun toggleFavorite(entity: FavoriteAircraftEntity): Boolean =
        favoriteMutex.withLock {
            withContext(ioDispatcher) {
                val normalized = entity.icaoTypeCode.uppercase()
                val exists = aircraftDao.isFavorite(normalized) > 0
                if (exists) {
                    aircraftDao.deleteFavorite(normalized)
                    false
                } else {
                    aircraftDao.insertFavorite(entity.copy(icaoTypeCode = normalized))
                    true
                }
            }
        }
}
