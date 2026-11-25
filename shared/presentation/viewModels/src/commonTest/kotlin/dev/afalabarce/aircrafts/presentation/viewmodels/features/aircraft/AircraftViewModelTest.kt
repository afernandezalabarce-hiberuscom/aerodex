package dev.afalabarce.aircrafts.presentation.viewmodels.features.aircraft

import dev.afalabarce.aircrafts.domain.models.AircraftCategory
import dev.afalabarce.aircrafts.domain.models.AircraftDetailResult
import dev.afalabarce.aircrafts.domain.models.AircraftModelDetail
import dev.afalabarce.aircrafts.domain.models.AircraftModelSummary
import dev.afalabarce.aircrafts.domain.models.AircraftSearchResult
import dev.afalabarce.aircrafts.domain.models.DataSourceOrigin
import dev.afalabarce.aircrafts.domain.repository.features.aircraft.AircraftRepository
import dev.afalabarce.aircrafts.domain.usecases.features.aircraft.GetAircraftModelDetailUseCase
import dev.afalabarce.aircrafts.domain.usecases.features.aircraft.ObserveFavoriteCodesUseCase
import dev.afalabarce.aircrafts.domain.usecases.features.aircraft.ObserveFavoritesUseCase
import dev.afalabarce.aircrafts.domain.usecases.features.aircraft.SearchAircraftModelsUseCase
import dev.afalabarce.aircrafts.domain.usecases.features.aircraft.ToggleFavoriteAircraftUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class FakePresentationAircraftRepository : AircraftRepository {
    var delayByQuery: Map<String, Long> = emptyMap()
    var originToReturn: DataSourceOrigin = DataSourceOrigin.NETWORK
    val completedQueries = mutableListOf<String>()

    override suspend fun searchAircraftModels(
        query: String,
        category: AircraftCategory?,
        cursor: String?,
        limit: Int
    ): AircraftSearchResult {
        delayByQuery[query]?.let { delay(it) }
        completedQueries.add(query)
        val item = AircraftModelSummary(
            icaoTypeCode = query.ifBlank { "A320" }.uppercase(),
            manufacturer = "Test",
            model = query.ifBlank { "A320" },
            name = "Model ${query.ifBlank { "A320" }}",
            category = category ?: AircraftCategory.COMMERCIAL
        )
        return AircraftSearchResult(
            items = listOf(item),
            nextCursor = null,
            hasMore = false,
            origin = originToReturn
        )
    }

    override suspend fun getAircraftModelDetail(icaoTypeCode: String): AircraftDetailResult {
        return AircraftDetailResult(
            detail = AircraftModelDetail(
                icaoTypeCode = icaoTypeCode,
                name = "Model $icaoTypeCode",
                category = AircraftCategory.COMMERCIAL
            ),
            origin = originToReturn
        )
    }

    override fun observeFavoriteCodes(): Flow<Set<String>> = flowOf(emptySet())

    override fun observeFavorites(): Flow<List<AircraftModelSummary>> = flowOf(emptyList())

    override suspend fun toggleFavorite(aircraft: AircraftModelSummary): Boolean = true
}

@OptIn(ExperimentalCoroutinesApi::class)
class AircraftViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        repository: AircraftRepository,
        autoLoadInitial: Boolean = false
    ): AircraftViewModel = AircraftViewModel(
        searchAircraftModelsUseCase = SearchAircraftModelsUseCase(repository),
        getAircraftModelDetailUseCase = GetAircraftModelDetailUseCase(repository),
        observeFavoriteCodesUseCase = ObserveFavoriteCodesUseCase(repository),
        observeFavoritesUseCase = ObserveFavoritesUseCase(repository),
        toggleFavoriteAircraftUseCase = ToggleFavoriteAircraftUseCase(repository),
        debounceMs = 0L,
        autoLoadInitial = autoLoadInitial
    )

    @Test
    fun newSearchCancelsPreviousInFlightSearchAndNeverShowsStaleResults() = runTest(testDispatcher) {
        val repository = FakePresentationAircraftRepository().apply {
            delayByQuery = mapOf(
                "A320" to 1_000L,
                "B737" to 100L
            )
        }
        val viewModel = createViewModel(repository, autoLoadInitial = false)

        // Start slow search
        viewModel.onQueryChanged("A320")
        advanceTimeBy(200L) // A320 is in-flight (needs 1000ms)

        // Start newer fast search before A320 finishes
        viewModel.onQueryChanged("B737")
        advanceUntilIdle()

        // Only B737 should have completed and be displayed
        assertEquals(listOf("B737"), repository.completedQueries)
        assertEquals("B737", viewModel.uiState.value.items.single().icaoTypeCode)
        assertNull(viewModel.uiState.value.searchError)
    }

    @Test
    fun selectAircraftLoadsDetailAndClearSelectionResetsIt() = runTest(testDispatcher) {
        val repository = FakePresentationAircraftRepository()
        val viewModel = createViewModel(repository, autoLoadInitial = false)

        viewModel.selectAircraft("A35K")
        advanceUntilIdle()

        assertEquals("A35K", viewModel.uiState.value.selectedIcaoCode)
        assertEquals("A35K", viewModel.uiState.value.selectedDetail?.icaoTypeCode)

        viewModel.clearSelection()
        assertNull(viewModel.uiState.value.selectedIcaoCode)
        assertNull(viewModel.uiState.value.selectedDetail)
    }

    @Test
    fun offlineOriginSetsIsOfflineDataFlag() = runTest(testDispatcher) {
        val repository = FakePresentationAircraftRepository().apply {
            originToReturn = DataSourceOrigin.OFFLINE_CACHE
        }
        val viewModel = createViewModel(repository, autoLoadInitial = true)

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isOfflineData)
        assertEquals(DataSourceOrigin.OFFLINE_CACHE, viewModel.uiState.value.searchOrigin)
    }

    @Test
    fun toggleCompareReusesSelectedDetailAndFetchesMissingSlot() = runTest(testDispatcher) {
        var detailCalls = 0
        val repository = object : AircraftRepository by FakePresentationAircraftRepository() {
            override suspend fun getAircraftModelDetail(icaoTypeCode: String): AircraftDetailResult {
                detailCalls++
                return AircraftDetailResult(
                    detail = AircraftModelDetail(
                        icaoTypeCode = icaoTypeCode,
                        name = "Model $icaoTypeCode",
                        category = AircraftCategory.COMMERCIAL
                    ),
                    origin = DataSourceOrigin.NETWORK
                )
            }
        }
        val viewModel = createViewModel(repository, autoLoadInitial = false)

        viewModel.selectAircraft("A359")
        advanceUntilIdle()
        assertEquals(1, detailCalls)

        // Adding A359 to compare should reuse selectedDetail without an extra detail fetch
        viewModel.toggleCompare("A359")
        advanceUntilIdle()
        assertEquals(1, detailCalls)
        assertEquals("A359", viewModel.uiState.value.compareLeftDetail?.icaoTypeCode)

        // Adding B789 should only fetch B789 (1 additional call, not 2)
        viewModel.toggleCompare("B789")
        advanceUntilIdle()
        assertEquals(2, detailCalls)
        assertEquals("A359", viewModel.uiState.value.compareLeftDetail?.icaoTypeCode)
        assertEquals("B789", viewModel.uiState.value.compareRightDetail?.icaoTypeCode)
    }
}
