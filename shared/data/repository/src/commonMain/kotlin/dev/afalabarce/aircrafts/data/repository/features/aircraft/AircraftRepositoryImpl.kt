package dev.afalabarce.aircrafts.data.repository.features.aircraft

import dev.afalabarce.aircrafts.core.common.coroutines.KeyedMutex
import dev.afalabarce.aircrafts.data.datasources.features.aircraft.AircraftLocalDataSource
import dev.afalabarce.aircrafts.data.datasources.features.aircraft.AircraftRemoteDataSource
import dev.afalabarce.aircrafts.data.repository.features.aircraft.cache.AircraftMemoryCache
import dev.afalabarce.aircrafts.data.repository.features.aircraft.cache.SearchCacheKey
import dev.afalabarce.aircrafts.data.repository.features.aircraft.mappers.toCacheEntity
import dev.afalabarce.aircrafts.data.repository.features.aircraft.mappers.toDomain
import dev.afalabarce.aircrafts.data.repository.features.aircraft.mappers.toFavoriteEntity
import dev.afalabarce.aircrafts.domain.models.AircraftCategory
import dev.afalabarce.aircrafts.domain.models.AircraftDetailResult
import dev.afalabarce.aircrafts.domain.models.AircraftModelSummary
import dev.afalabarce.aircrafts.domain.models.AircraftSearchResult
import dev.afalabarce.aircrafts.domain.models.DataSourceOrigin
import dev.afalabarce.aircrafts.domain.repository.features.aircraft.AircraftRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Clock

/**
 * Offline-first repository for aircraft models, details, and favorites.
 *
 * Threading, Cancellation & Mutual Exclusion decisions:
 * - [ioDispatcher] (defaults to [Dispatchers.IO]) isolates DTO-to-domain mapping, cache writes,
 *   and offline SQLite fallbacks from `Dispatchers.Main`.
 * - [searchLocks] and [detailLocks] ([KeyedMutex]) provide per-key mutual exclusion: concurrent
 *   requests for the *same* search key or ICAO code coalesce (the second hits [AircraftMemoryCache]
 *   after the first completes), while *distinct* keys (e.g. parallel comparator fetches) execute
 *   concurrently without blocking each other.
 * - [CancellationException] is always rethrown immediately so structured cancellation propagates
 *   cleanly without triggering offline fallback or swallowing job cancellation.
 */
class AircraftRepositoryImpl(
    private val remoteDataSource: AircraftRemoteDataSource,
    private val localDataSource: AircraftLocalDataSource,
    private val memoryCache: AircraftMemoryCache,
    private val clockMs: () -> Long = { Clock.System.now().toEpochMilliseconds() },
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AircraftRepository {

    private val searchLocks = KeyedMutex<SearchCacheKey>()
    private val detailLocks = KeyedMutex<String>()

    override suspend fun searchAircraftModels(
        query: String,
        category: AircraftCategory?,
        cursor: String?,
        limit: Int
    ): AircraftSearchResult {
        val normalizedQuery = query.trim()
        val cacheKey = SearchCacheKey(
            query = normalizedQuery.lowercase(),
            category = category,
            cursor = cursor,
            limit = limit
        )

        memoryCache.getSearch(cacheKey)?.let { cached ->
            return cached.copy(origin = DataSourceOrigin.MEMORY_CACHE)
        }

        return withContext(ioDispatcher) {
            searchLocks.withLock(cacheKey) {
                memoryCache.getSearch(cacheKey)?.let { cached ->
                    return@withLock cached.copy(origin = DataSourceOrigin.MEMORY_CACHE)
                }

                try {
                    val pageDto = remoteDataSource.searchModels(
                        query = normalizedQuery.ifEmpty { null },
                        category = category?.apiValue,
                        cursor = cursor,
                        limit = limit
                    )
                    val domainItems = pageDto.items.map { it.toDomain() }
                    val networkResult = AircraftSearchResult(
                        items = domainItems,
                        nextCursor = pageDto.nextCursor,
                        hasMore = pageDto.hasMore,
                        origin = DataSourceOrigin.NETWORK
                    )

                    memoryCache.putSearch(cacheKey, networkResult)
                    val now = clockMs()
                    persistSafely("Failed to persist aircraft summaries for offline cache") {
                        localDataSource.saveSummaries(domainItems.map { it.toCacheEntity(now) })
                    }

                    networkResult
                } catch (ce: CancellationException) {
                    throw ce
                } catch (error: Throwable) {
                    Napier.w("Network search failed, attempting offline cache fallback", error)
                    val offlineItems = if (cursor == null) {
                        localDataSource.searchCachedSummaries(
                            query = normalizedQuery,
                            category = category?.apiValue,
                            limit = limit
                        ).map { it.toDomain() }
                    } else {
                        emptyList()
                    }

                    if (offlineItems.isNotEmpty()) {
                        AircraftSearchResult(
                            items = offlineItems,
                            nextCursor = null,
                            hasMore = false,
                            origin = DataSourceOrigin.OFFLINE_CACHE
                        )
                    } else {
                        throw error
                    }
                }
            }
        }
    }

    override suspend fun getAircraftModelDetail(icaoTypeCode: String): AircraftDetailResult {
        val normalizedCode = icaoTypeCode.trim().uppercase()

        memoryCache.getDetail(normalizedCode)?.let { cached ->
            return AircraftDetailResult(
                detail = cached,
                origin = DataSourceOrigin.MEMORY_CACHE
            )
        }

        return withContext(ioDispatcher) {
            detailLocks.withLock(normalizedCode) {
                memoryCache.getDetail(normalizedCode)?.let { cached ->
                    return@withLock AircraftDetailResult(
                        detail = cached,
                        origin = DataSourceOrigin.MEMORY_CACHE
                    )
                }

                try {
                    val detailDto = remoteDataSource.getModelDetail(normalizedCode)
                    val domainDetail = detailDto.toDomain()

                    memoryCache.putDetail(normalizedCode, domainDetail)
                    val now = clockMs()
                    persistSafely("Failed to persist aircraft detail for offline cache") {
                        localDataSource.saveDetail(detailDto, now)
                    }

                    AircraftDetailResult(
                        detail = domainDetail,
                        origin = DataSourceOrigin.NETWORK
                    )
                } catch (ce: CancellationException) {
                    throw ce
                } catch (error: Throwable) {
                    Napier.w("Network detail failed for $normalizedCode, attempting offline cache fallback", error)
                    val offlineDetail = localDataSource.getCachedDetail(normalizedCode)?.toDomain()
                    if (offlineDetail != null) {
                        AircraftDetailResult(
                            detail = offlineDetail,
                            origin = DataSourceOrigin.OFFLINE_CACHE
                        )
                    } else {
                        throw error
                    }
                }
            }
        }
    }

    private suspend inline fun persistSafely(
        warningMessage: String,
        crossinline block: suspend () -> Unit
    ) {
        try {
            block()
        } catch (ce: CancellationException) {
            throw ce
        } catch (error: Throwable) {
            Napier.w(warningMessage, error)
        }
    }

    override fun observeFavoriteCodes(): Flow<Set<String>> =
        localDataSource.observeFavoriteCodes()

    override fun observeFavorites(): Flow<List<AircraftModelSummary>> =
        localDataSource.observeFavorites().map { entities -> entities.map { it.toDomain() } }

    override suspend fun toggleFavorite(aircraft: AircraftModelSummary): Boolean =
        withContext(ioDispatcher) {
            localDataSource.toggleFavorite(aircraft.toFavoriteEntity(clockMs()))
        }
}
