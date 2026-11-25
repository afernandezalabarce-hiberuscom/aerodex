package dev.afalabarce.aircrafts.data.repository.features.aircraft

import dev.afalabarce.aircrafts.data.datasources.features.aircraft.AircraftLocalDataSource
import dev.afalabarce.aircrafts.data.datasources.features.aircraft.AircraftRemoteDataSource
import dev.afalabarce.aircrafts.data.repository.features.aircraft.cache.AircraftMemoryCache
import dev.afalabarce.aircrafts.domain.models.DataSourceOrigin
import dev.afalabarce.aircrafts.models.features.aircraft.local.CachedAircraftSummaryEntity
import dev.afalabarce.aircrafts.models.features.aircraft.local.FavoriteAircraftEntity
import dev.afalabarce.aircrafts.models.features.aircraft.remote.AircraftModelDetailDto
import dev.afalabarce.aircrafts.models.features.aircraft.remote.AircraftModelSummaryDto
import dev.afalabarce.aircrafts.models.features.aircraft.remote.AircraftModelsPageDto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

private class FakeRemoteDataSource : AircraftRemoteDataSource {
    var searchCalls = 0
    var detailCalls = 0
    var errorToThrow: Throwable? = null
    var searchResponse = AircraftModelsPageDto(
        items = listOf(
            AircraftModelSummaryDto(
                icaoTypeCode = "A320",
                manufacturer = "Airbus",
                model = "A320",
                name = "Airbus A320",
                category = "COMMERCIAL"
            )
        ),
        nextCursor = null,
        hasMore = false,
        limit = 20
    )
    var detailResponse = AircraftModelDetailDto(
        icaoTypeCode = "A320",
        manufacturer = "Airbus",
        model = "A320",
        name = "Airbus A320",
        category = "COMMERCIAL"
    )

    override suspend fun searchModels(
        query: String?,
        category: String?,
        cursor: String?,
        limit: Int
    ): AircraftModelsPageDto {
        searchCalls++
        errorToThrow?.let { throw it }
        return searchResponse
    }

    override suspend fun getModelDetail(icaoTypeCode: String): AircraftModelDetailDto {
        detailCalls++
        errorToThrow?.let { throw it }
        return detailResponse
    }
}

private class FakeLocalDataSource : AircraftLocalDataSource {
    val summaries = mutableListOf<CachedAircraftSummaryEntity>()
    val details = mutableMapOf<String, AircraftModelDetailDto>()

    override suspend fun saveSummaries(summaries: List<CachedAircraftSummaryEntity>) {
        this.summaries.removeAll { existing -> summaries.any { it.icaoTypeCode == existing.icaoTypeCode } }
        this.summaries.addAll(summaries)
    }

    override suspend fun searchCachedSummaries(
        query: String,
        category: String?,
        limit: Int
    ): List<CachedAircraftSummaryEntity> {
        return summaries.filter {
            (category == null || it.category == category) &&
                (query.isBlank() || it.name.contains(query, ignoreCase = true) ||
                    it.icaoTypeCode.contains(query, ignoreCase = true))
        }.take(limit)
    }

    override suspend fun saveDetail(detail: AircraftModelDetailDto, updatedAtMs: Long) {
        details[detail.icaoTypeCode.uppercase()] = detail
    }

    override suspend fun getCachedDetail(icaoTypeCode: String): AircraftModelDetailDto? {
        return details[icaoTypeCode.uppercase()]
    }

    override fun observeFavoriteCodes(): Flow<Set<String>> = flowOf(emptySet())

    override fun observeFavorites(): Flow<List<FavoriteAircraftEntity>> = flowOf(emptyList())

    override suspend fun toggleFavorite(entity: FavoriteAircraftEntity): Boolean = true
}

class AircraftRepositoryCacheTest {

    @Test
    fun searchReturnsMemoryCacheWithinTtlWithoutCallingNetwork() = runTest {
        var nowMs = 1_000L
        val remote = FakeRemoteDataSource()
        val local = FakeLocalDataSource()
        val cache = AircraftMemoryCache(ttlMs = 180_000L, clockMs = { nowMs })
        val repository = AircraftRepositoryImpl(remote, local, cache, clockMs = { nowMs })

        val first = repository.searchAircraftModels(query = "A320")
        assertEquals(DataSourceOrigin.NETWORK, first.origin)
        assertEquals(1, remote.searchCalls)

        // Advance time within 3-minute TTL
        nowMs += 60_000L
        val second = repository.searchAircraftModels(query = "  a320 ")
        assertEquals(DataSourceOrigin.MEMORY_CACHE, second.origin)
        assertEquals(1, remote.searchCalls)
    }

    @Test
    fun searchRefreshesFromNetworkAfterTtlExpires() = runTest {
        var nowMs = 1_000L
        val remote = FakeRemoteDataSource()
        val local = FakeLocalDataSource()
        val cache = AircraftMemoryCache(ttlMs = 180_000L, clockMs = { nowMs })
        val repository = AircraftRepositoryImpl(remote, local, cache, clockMs = { nowMs })

        val first = repository.searchAircraftModels(query = "A320")
        assertEquals(DataSourceOrigin.NETWORK, first.origin)
        assertEquals(1, remote.searchCalls)

        // Advance time past TTL (3 minutes + 1 ms)
        nowMs += 180_001L
        val refreshed = repository.searchAircraftModels(query = "A320")
        assertEquals(DataSourceOrigin.NETWORK, refreshed.origin)
        assertEquals(2, remote.searchCalls)
    }

    @Test
    fun searchFallsBackToOfflineLocalCacheWhenNetworkFails() = runTest {
        var nowMs = 1_000L
        val remote = FakeRemoteDataSource()
        val local = FakeLocalDataSource()
        val cache = AircraftMemoryCache(ttlMs = 180_000L, clockMs = { nowMs })
        val repository = AircraftRepositoryImpl(remote, local, cache, clockMs = { nowMs })

        // Populate local cache via initial online call
        repository.searchAircraftModels(query = "A320")
        assertEquals(1, local.summaries.size)

        // Expire memory cache and simulate offline network error
        nowMs += 200_000L
        remote.errorToThrow = IllegalStateException("No internet connection")

        val offlineResult = repository.searchAircraftModels(query = "A320")
        assertEquals(DataSourceOrigin.OFFLINE_CACHE, offlineResult.origin)
        assertEquals("A320", offlineResult.items.first().icaoTypeCode)
    }

    @Test
    fun cancellationExceptionIsRethrownWithoutOfflineFallback() = runTest {
        val remote = FakeRemoteDataSource().apply {
            errorToThrow = CancellationException("Cancelled by newer search")
        }
        val local = FakeLocalDataSource().apply {
            summaries.add(
                CachedAircraftSummaryEntity(
                    icaoTypeCode = "A320",
                    name = "Airbus A320",
                    category = "COMMERCIAL"
                )
            )
        }
        val repository = AircraftRepositoryImpl(remote, local, AircraftMemoryCache())

        assertFailsWith<CancellationException> {
            repository.searchAircraftModels(query = "A320")
        }
    }

    @Test
    fun concurrentDetailRequestsForSameCodeCoalesceViaKeyedMutex() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val slowRemote = object : AircraftRemoteDataSource {
            var calls = 0
            override suspend fun searchModels(
                query: String?,
                category: String?,
                cursor: String?,
                limit: Int
            ) = FakeRemoteDataSource().searchResponse

            override suspend fun getModelDetail(icaoTypeCode: String): AircraftModelDetailDto {
                calls++
                delay(100L)
                return AircraftModelDetailDto(
                    icaoTypeCode = icaoTypeCode,
                    name = "Model $icaoTypeCode",
                    category = "COMMERCIAL"
                )
            }
        }
        val repository = AircraftRepositoryImpl(
            remoteDataSource = slowRemote,
            localDataSource = FakeLocalDataSource(),
            memoryCache = AircraftMemoryCache(),
            ioDispatcher = dispatcher
        )

        val (first, second) = listOf(
            async { repository.getAircraftModelDetail("A350") },
            async { repository.getAircraftModelDetail("a350") }
        ).awaitAll()

        assertEquals(1, slowRemote.calls)
        assertEquals(DataSourceOrigin.NETWORK, first.origin)
        assertEquals(DataSourceOrigin.MEMORY_CACHE, second.origin)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun cancellingOneConcurrentCallerDoesNotBreakSecondCaller() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val slowRemote = object : AircraftRemoteDataSource {
            var calls = 0
            override suspend fun searchModels(
                query: String?,
                category: String?,
                cursor: String?,
                limit: Int
            ) = FakeRemoteDataSource().searchResponse

            override suspend fun getModelDetail(icaoTypeCode: String): AircraftModelDetailDto {
                calls++
                delay(100L)
                return AircraftModelDetailDto(
                    icaoTypeCode = icaoTypeCode,
                    name = "Model $icaoTypeCode",
                    category = "COMMERCIAL"
                )
            }
        }
        val repository = AircraftRepositoryImpl(
            remoteDataSource = slowRemote,
            localDataSource = FakeLocalDataSource(),
            memoryCache = AircraftMemoryCache(),
            ioDispatcher = dispatcher
        )

        val firstJob = async { repository.getAircraftModelDetail("B787") }
        val secondJob = async { repository.getAircraftModelDetail("B787") }

        advanceTimeBy(20L)
        firstJob.cancel()

        val secondResult = secondJob.await()
        assertEquals("B787", secondResult.detail.icaoTypeCode)
        assertEquals(2, slowRemote.calls)
    }
}
