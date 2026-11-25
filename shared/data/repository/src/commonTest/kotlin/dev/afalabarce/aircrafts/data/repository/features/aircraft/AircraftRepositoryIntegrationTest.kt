package dev.afalabarce.aircrafts.data.repository.features.aircraft

import de.jensklingenberg.ktorfit.Ktorfit
import dev.afalabarce.aircrafts.data.datasources.core.features.aircraft.AircraftRemoteDataSourceImpl
import dev.afalabarce.aircrafts.data.datasources.core.remote.ApiService
import dev.afalabarce.aircrafts.data.datasources.core.remote.createApiService
import dev.afalabarce.aircrafts.data.datasources.features.aircraft.AircraftLocalDataSource
import dev.afalabarce.aircrafts.data.repository.features.aircraft.cache.AircraftMemoryCache
import dev.afalabarce.aircrafts.domain.models.AircraftCategory
import dev.afalabarce.aircrafts.domain.models.DataSourceOrigin
import dev.afalabarce.aircrafts.models.features.aircraft.local.CachedAircraftSummaryEntity
import dev.afalabarce.aircrafts.models.features.aircraft.local.FavoriteAircraftEntity
import dev.afalabarce.aircrafts.models.features.aircraft.remote.AircraftModelDetailDto
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private class InMemoryIntegrationLocalDataSource : AircraftLocalDataSource {
    private val summaries = mutableListOf<CachedAircraftSummaryEntity>()
    private val details = mutableMapOf<String, AircraftModelDetailDto>()
    private val favoritesFlow = MutableStateFlow<List<FavoriteAircraftEntity>>(emptyList())

    override suspend fun saveSummaries(summaries: List<CachedAircraftSummaryEntity>) {
        this.summaries.removeAll { existing -> summaries.any { it.icaoTypeCode == existing.icaoTypeCode } }
        this.summaries.addAll(summaries)
    }

    override suspend fun searchCachedSummaries(
        query: String,
        category: String?,
        limit: Int
    ): List<CachedAircraftSummaryEntity> = summaries.filter {
        (category == null || it.category == category) &&
            (query.isBlank() || it.name.contains(query, ignoreCase = true) ||
                it.icaoTypeCode.contains(query, ignoreCase = true))
    }.take(limit)

    override suspend fun saveDetail(detail: AircraftModelDetailDto, updatedAtMs: Long) {
        details[detail.icaoTypeCode.uppercase()] = detail
    }

    override suspend fun getCachedDetail(icaoTypeCode: String): AircraftModelDetailDto? =
        details[icaoTypeCode.uppercase()]

    override fun observeFavoriteCodes(): Flow<Set<String>> =
        favoritesFlow.map { list -> list.map { it.icaoTypeCode.uppercase() }.toSet() }

    override fun observeFavorites(): Flow<List<FavoriteAircraftEntity>> = favoritesFlow

    override suspend fun toggleFavorite(entity: FavoriteAircraftEntity): Boolean {
        val current = favoritesFlow.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.icaoTypeCode.equals(entity.icaoTypeCode, ignoreCase = true) }
        return if (existingIndex >= 0) {
            current.removeAt(existingIndex)
            favoritesFlow.value = current
            false
        } else {
            current.add(entity)
            favoritesFlow.value = current
            true
        }
    }
}

class AircraftRepositoryIntegrationTest {

    private fun createRepository(
        shouldFailNetwork: () -> Boolean = { false }
    ): Pair<AircraftRepositoryImpl, InMemoryIntegrationLocalDataSource> {
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
        val mockEngine = MockEngine { request ->
            if (shouldFailNetwork()) {
                return@MockEngine respond(
                    content = """{"error":"Network unavailable"}""",
                    status = HttpStatusCode.ServiceUnavailable,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
            val path = request.url.encodedPath
            when {
                path.endsWith("/api/v1/models") -> {
                    respond(
                        content = """
                            {
                              "items": [
                                {
                                  "icaoTypeCode": "A359",
                                  "iataTypeCode": "359",
                                  "manufacturer": "Airbus",
                                  "model": "A350-900",
                                  "name": "Airbus A350-900",
                                  "category": "COMMERCIAL",
                                  "maxPassengers": 350,
                                  "cruiseSpeedKt": 488,
                                  "rangeNm": 8100
                                }
                              ],
                              "nextCursor": "cursor_2",
                              "hasMore": true,
                              "limit": 20
                            }
                        """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }

                path.endsWith("/api/v1/models/A359") -> {
                    respond(
                        content = """
                            {
                              "icaoTypeCode": "A359",
                              "iataTypeCode": "359",
                              "manufacturer": "Airbus",
                              "model": "A350-900",
                              "name": "Airbus A350-900",
                              "category": "COMMERCIAL",
                              "countryOfOrigin": "France",
                              "firstFlightYear": 2013,
                              "fleetCount": 600,
                              "performance": {
                                "cruiseSpeedKt": 488,
                                "rangeNm": 8100
                              },
                              "capacity": {
                                "maxPassengers": 350
                              }
                            }
                        """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }

                else -> respond(
                    content = """{"error":"Not found"}""",
                    status = HttpStatusCode.NotFound,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(json)
            }
        }
        val ktorfit = Ktorfit.Builder()
            .baseUrl(ApiService.API_URL)
            .httpClient(httpClient)
            .build()
        val apiService = ktorfit.createApiService()
        val remoteDataSource = AircraftRemoteDataSourceImpl(apiService)
        val localDataSource = InMemoryIntegrationLocalDataSource()
        val memoryCache = AircraftMemoryCache(
            ttlMs = 180_000L,
            clockMs = { 1_000_000L }
        )
        val repository = AircraftRepositoryImpl(
            remoteDataSource = remoteDataSource,
            localDataSource = localDataSource,
            memoryCache = memoryCache
        )
        return repository to localDataSource
    }

    @Test
    fun `search and detail integrate Ktorfit MockEngine mapping memory cache and offline fallback`() = runTest {
        var failNetwork = false
        val (repository, localDataSource) = createRepository { failNetwork }

        // 1. Live network search via Ktorfit + MockEngine
        val networkSearch = repository.searchAircraftModels(
            query = "A350",
            category = AircraftCategory.COMMERCIAL,
            cursor = null,
            limit = 20
        )
        assertEquals(DataSourceOrigin.NETWORK, networkSearch.origin)
        assertEquals(1, networkSearch.items.size)
        assertEquals("A359", networkSearch.items.first().icaoTypeCode)
        assertTrue(networkSearch.hasMore)
        assertEquals("cursor_2", networkSearch.nextCursor)

        // 2. Second identical search hits TTL memory cache
        val cachedSearch = repository.searchAircraftModels(
            query = "A350",
            category = AircraftCategory.COMMERCIAL,
            cursor = null,
            limit = 20
        )
        assertEquals(DataSourceOrigin.MEMORY_CACHE, cachedSearch.origin)

        // 3. Detail fetch via Ktorfit + MockEngine persists offline
        val detail = repository.getAircraftModelDetail("A359")
        assertEquals(DataSourceOrigin.NETWORK, detail.origin)
        assertEquals("Airbus A350-900", detail.detail.name)
        assertEquals(8100, detail.detail.performance?.rangeNm)
        assertEquals(350, detail.detail.capacity?.maxPassengers)

        // 4. New repository sharing localDataSource simulates cold start offline fallback
        failNetwork = true
        val offlineRepository = AircraftRepositoryImpl(
            remoteDataSource = createRepository { true }.first.let {
                // Force network error with same localDataSource
                object : dev.afalabarce.aircrafts.data.datasources.features.aircraft.AircraftRemoteDataSource {
                    override suspend fun searchModels(
                        query: String?,
                        category: String?,
                        cursor: String?,
                        limit: Int
                    ) = error("Offline")

                    override suspend fun getModelDetail(icaoTypeCode: String) = error("Offline")
                }
            },
            localDataSource = localDataSource,
            memoryCache = AircraftMemoryCache(
                ttlMs = 180_000L,
                clockMs = { 2_000_000L }
            )
        )

        val offlineDetail = offlineRepository.getAircraftModelDetail("A359")
        assertEquals(DataSourceOrigin.OFFLINE_CACHE, offlineDetail.origin)
        assertEquals("Airbus A350-900", offlineDetail.detail.name)
    }

    @Test
    fun `favorites toggle persists and emits reactive flow updates`() = runTest {
        val (repository, _) = createRepository()
        val search = repository.searchAircraftModels("", null, null, 20)
        val item = search.items.first()

        val added = repository.toggleFavorite(item)
        assertTrue(added)
        assertEquals(setOf("A359"), repository.observeFavoriteCodes().first())
        assertEquals(1, repository.observeFavorites().first().size)

        val removed = repository.toggleFavorite(item)
        assertFalse(removed)
        assertEquals(emptySet(), repository.observeFavoriteCodes().first())
    }
}
