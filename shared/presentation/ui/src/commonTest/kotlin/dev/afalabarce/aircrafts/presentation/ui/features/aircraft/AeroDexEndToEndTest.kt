package dev.afalabarce.aircrafts.presentation.ui.features.aircraft

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.jensklingenberg.ktorfit.Ktorfit
import dev.afalabarce.aircrafts.core.ui.theme.AppMaterialTheme
import dev.afalabarce.aircrafts.presentation.ui.common.CommonRunWith
import dev.afalabarce.aircrafts.presentation.ui.common.PlatformTestRunner
import dev.afalabarce.aircrafts.data.datasources.core.features.aircraft.AircraftRemoteDataSourceImpl
import dev.afalabarce.aircrafts.data.datasources.core.remote.ApiService
import dev.afalabarce.aircrafts.data.datasources.core.remote.createApiService
import dev.afalabarce.aircrafts.data.datasources.features.aircraft.AircraftLocalDataSource
import dev.afalabarce.aircrafts.data.repository.features.aircraft.AircraftRepositoryImpl
import dev.afalabarce.aircrafts.data.repository.features.aircraft.cache.AircraftMemoryCache
import dev.afalabarce.aircrafts.domain.usecases.features.aircraft.GetAircraftModelDetailUseCase
import dev.afalabarce.aircrafts.domain.usecases.features.aircraft.ObserveFavoriteCodesUseCase
import dev.afalabarce.aircrafts.domain.usecases.features.aircraft.ObserveFavoritesUseCase
import dev.afalabarce.aircrafts.domain.usecases.features.aircraft.SearchAircraftModelsUseCase
import dev.afalabarce.aircrafts.domain.usecases.features.aircraft.ToggleFavoriteAircraftUseCase
import dev.afalabarce.aircrafts.models.features.aircraft.local.CachedAircraftSummaryEntity
import dev.afalabarce.aircrafts.models.features.aircraft.local.FavoriteAircraftEntity
import dev.afalabarce.aircrafts.models.features.aircraft.remote.AircraftModelDetailDto
import dev.afalabarce.aircrafts.presentation.viewmodels.features.aircraft.AeroDexTab
import dev.afalabarce.aircrafts.presentation.viewmodels.features.aircraft.AircraftViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.Res
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.action_compare_add_cd
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.action_favorite_add_cd
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.back
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.comparator_title
import org.jetbrains.compose.resources.stringResource

private class E2eLocalDataSource : AircraftLocalDataSource {
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
    ): List<CachedAircraftSummaryEntity> = summaries.take(limit)

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
        val idx = current.indexOfFirst { it.icaoTypeCode.equals(entity.icaoTypeCode, ignoreCase = true) }
        return if (idx >= 0) {
            current.removeAt(idx)
            favoritesFlow.value = current
            false
        } else {
            current.add(entity)
            favoritesFlow.value = current
            true
        }
    }
}

@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
@CommonRunWith(PlatformTestRunner::class)
class AeroDexEndToEndTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createEndToEndViewModel(): AircraftViewModel {
        val mockEngine = MockEngine { request ->
            val path = request.url.encodedPath
            when {
                path.endsWith("/api/v1/models") -> respond(
                    content = """
                        {
                          "items": [
                            {
                              "icaoTypeCode": "A320",
                              "manufacturer": "Airbus",
                              "model": "A320-200",
                              "name": "Airbus A320",
                              "category": "COMMERCIAL",
                              "maxPassengers": 180,
                              "rangeNm": 3300
                            },
                            {
                              "icaoTypeCode": "B789",
                              "manufacturer": "Boeing",
                              "model": "787-9",
                              "name": "Boeing 787-9 Dreamliner",
                              "category": "COMMERCIAL",
                              "maxPassengers": 296,
                              "rangeNm": 7635
                            }
                          ],
                          "hasMore": false,
                          "limit": 20
                        }
                    """.trimIndent(),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )

                path.endsWith("/api/v1/models/A320") -> respond(
                    content = """
                        {
                          "icaoTypeCode": "A320",
                          "manufacturer": "Airbus",
                          "model": "A320-200",
                          "name": "Airbus A320",
                          "category": "COMMERCIAL",
                          "countryOfOrigin": "France",
                          "firstFlightYear": 1987,
                          "performance": { "cruiseSpeedKt": 447, "rangeNm": 3300 },
                          "capacity": { "maxPassengers": 180 }
                        }
                    """.trimIndent(),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )

                path.endsWith("/api/v1/models/B789") -> respond(
                    content = """
                        {
                          "icaoTypeCode": "B789",
                          "manufacturer": "Boeing",
                          "model": "787-9",
                          "name": "Boeing 787-9 Dreamliner",
                          "category": "COMMERCIAL",
                          "countryOfOrigin": "United States",
                          "firstFlightYear": 2013,
                          "performance": { "cruiseSpeedKt": 488, "rangeNm": 7635 },
                          "capacity": { "maxPassengers": 296 }
                        }
                    """.trimIndent(),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )

                else -> respond(
                    content = """{"error":"Not found"}""",
                    status = HttpStatusCode.NotFound,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        val apiService = Ktorfit.Builder()
            .baseUrl(ApiService.API_URL)
            .httpClient(httpClient)
            .build()
            .createApiService()

        val repository = AircraftRepositoryImpl(
            remoteDataSource = AircraftRemoteDataSourceImpl(apiService),
            localDataSource = E2eLocalDataSource(),
            memoryCache = AircraftMemoryCache(
                ttlMs = 180_000L,
                clockMs = { 1_000_000L }
            )
        )

        return AircraftViewModel(
            searchAircraftModelsUseCase = SearchAircraftModelsUseCase(repository),
            getAircraftModelDetailUseCase = GetAircraftModelDetailUseCase(repository),
            observeFavoriteCodesUseCase = ObserveFavoriteCodesUseCase(repository),
            observeFavoritesUseCase = ObserveFavoritesUseCase(repository),
            toggleFavoriteAircraftUseCase = ToggleFavoriteAircraftUseCase(repository),
            autoLoadInitial = false
        )
    }

    @Test
    fun `complete user journey from search to detail favorites and comparator`() =
        runComposeUiTest {
        val viewModel = createEndToEndViewModel()
        viewModel.retrySearch()

        lateinit var addA320FavCd: String
        lateinit var addA320CompareCd: String
        lateinit var backCd: String
        lateinit var addB789CompareCd: String
        lateinit var comparatorTitle: String

        setContent {
            addA320FavCd = stringResource(Res.string.action_favorite_add_cd, "Airbus A320")
            addA320CompareCd = stringResource(Res.string.action_compare_add_cd, "Airbus A320")
            backCd = stringResource(Res.string.back)
            addB789CompareCd = stringResource(Res.string.action_compare_add_cd, "Boeing 787-9 Dreamliner")
            comparatorTitle = stringResource(Res.string.comparator_title)
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            AppMaterialTheme {
                Column {
                    when {
                        state.selectedTab == AeroDexTab.FAVORITES -> FavoritesPane(
                            uiState = state,
                            bottomContentPadding = 0.dp,
                            onAircraftClick = viewModel::selectAircraft,
                            onToggleFavorite = viewModel::toggleFavorite,
                            onToggleCompare = viewModel::toggleCompare
                        )

                        state.selectedTab == AeroDexTab.COMPARATOR -> ComparatorPane(
                            uiState = state,
                            bottomContentPadding = 0.dp,
                            onSwap = viewModel::swapCompare,
                            onClear = viewModel::clearCompare,
                            onExploreClick = { viewModel.selectTab(AeroDexTab.EXPLORE) }
                        )

                        state.selectedIcaoCode != null -> AircraftDetailPane(
                            uiState = state,
                            showBackButton = true,
                            onBack = viewModel::clearSelection,
                            onRetry = viewModel::retryDetail,
                            onToggleFavorite = viewModel::toggleFavoriteFromDetail,
                            onToggleCompare = viewModel::toggleCompare
                        )

                        else -> AircraftSearchPane(
                            uiState = state,
                            onQueryChange = viewModel::onQueryChanged,
                            onSearchSubmit = viewModel::submitSearch,
                            onClearQuery = viewModel::clearQuery,
                            onCategorySelected = viewModel::onCategorySelected,
                            onAircraftClick = viewModel::selectAircraft,
                            onLoadMore = viewModel::loadMore,
                            onRetry = viewModel::retrySearch,
                            onToggleFavorite = viewModel::toggleFavorite,
                            onToggleCompare = viewModel::toggleCompare
                        )
                    }
                }
            }
        }

        waitUntil(timeoutMillis = 5_000L) {
            runBlocking { delay(20) }
            !viewModel.uiState.value.isLoading &&
                (viewModel.uiState.value.items.isNotEmpty() || viewModel.uiState.value.searchError != null)
        }
        assertEquals(null, viewModel.uiState.value.searchError)
        waitForIdle()
        // 1. Verify initial search results from MockEngine API
        onNodeWithText("Airbus A320").assertIsDisplayed()

        val verticalGrid = SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange)
        // 2. Favorite Airbus A320, add to comparator, and open its detail
        onNodeWithContentDescription(addA320FavCd).performScrollTo().performClick()
        waitUntil(timeoutMillis = 5_000L) {
            runBlocking { delay(20) }
            viewModel.uiState.value.favorites.isNotEmpty()
        }
        onNodeWithContentDescription(addA320CompareCd).performScrollTo().performClick()
        onNodeWithText("Airbus A320").performScrollTo().performClick()
        waitUntil(timeoutMillis = 5_000L) {
            runBlocking { delay(20) }
            !viewModel.uiState.value.isDetailLoading &&
                (viewModel.uiState.value.selectedDetail != null || viewModel.uiState.value.detailError != null)
        }
        assertEquals(null, viewModel.uiState.value.detailError)
        waitForIdle()
        onNodeWithText("A320").assertIsDisplayed()

        // 3. Return to list, scroll to Boeing 787-9 Dreamliner, and add to comparator
        onNodeWithContentDescription(backCd).performClick()
        waitForIdle()
        onNode(verticalGrid).performScrollToNode(hasText("Boeing 787-9 Dreamliner"))
        onNodeWithText("Boeing 787-9 Dreamliner").assertIsDisplayed()
        onNodeWithContentDescription(addB789CompareCd).performScrollTo().performClick()

        // 4. Switch to Favorites tab and verify Airbus A320 is persisted
        viewModel.selectTab(AeroDexTab.FAVORITES)
        waitUntil(timeoutMillis = 5_000L) {
            runBlocking { delay(20) }
            viewModel.uiState.value.favorites.isNotEmpty()
        }
        waitForIdle()
        onNodeWithText("Airbus A320").assertIsDisplayed()

        // 5. Switch to Comparator tab and verify side-by-side comparison loaded
        viewModel.selectTab(AeroDexTab.COMPARATOR)
        waitUntil(timeoutMillis = 5_000L) {
            runBlocking { delay(20) }
            viewModel.uiState.value.compareLeftDetail != null
        }
        waitForIdle()
        assertEquals(listOf("A320", "B789"), viewModel.uiState.value.compareCodes)
        onNodeWithText(comparatorTitle).assertIsDisplayed()
    }
}
