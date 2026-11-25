package dev.afalabarce.aircrafts.presentation.viewmodels.features.aircraft

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.afalabarce.aircrafts.domain.models.AircraftCategory
import dev.afalabarce.aircrafts.domain.models.AircraftModelDetail
import dev.afalabarce.aircrafts.domain.models.AircraftModelSummary
import dev.afalabarce.aircrafts.domain.models.DataSourceOrigin
import dev.afalabarce.aircrafts.domain.usecases.features.aircraft.GetAircraftModelDetailUseCase
import dev.afalabarce.aircrafts.domain.usecases.features.aircraft.ObserveFavoriteCodesUseCase
import dev.afalabarce.aircrafts.domain.usecases.features.aircraft.ObserveFavoritesUseCase
import dev.afalabarce.aircrafts.domain.usecases.features.aircraft.SearchAircraftModelsUseCase
import dev.afalabarce.aircrafts.domain.usecases.features.aircraft.ToggleFavoriteAircraftUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AeroDexTab {
    EXPLORE,
    FAVORITES,
    COMPARATOR,
    PROFILE
}

sealed interface AircraftError {
    data class Message(val detail: String) : AircraftError
    data object SearchFailed : AircraftError
    data object DetailFailed : AircraftError
    data object CompareFailed : AircraftError
}

data class AircraftUiState(
    val selectedTab: AeroDexTab = AeroDexTab.EXPLORE,
    val query: String = "",
    val selectedCategory: AircraftCategory? = null,
    val items: List<AircraftModelSummary> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = false,
    val nextCursor: String? = null,
    val searchOrigin: DataSourceOrigin? = null,
    val searchError: AircraftError? = null,
    val selectedIcaoCode: String? = null,
    val selectedDetail: AircraftModelDetail? = null,
    val detailOrigin: DataSourceOrigin? = null,
    val isDetailLoading: Boolean = false,
    val detailError: AircraftError? = null,
    val favoriteCodes: Set<String> = emptySet(),
    val favorites: List<AircraftModelSummary> = emptyList(),
    val compareCodes: List<String> = emptyList(),
    val compareLeftDetail: AircraftModelDetail? = null,
    val compareRightDetail: AircraftModelDetail? = null,
    val isCompareLoading: Boolean = false,
    val compareError: AircraftError? = null
) {
    val isOfflineData: Boolean
        get() = searchOrigin == DataSourceOrigin.OFFLINE_CACHE ||
            detailOrigin == DataSourceOrigin.OFFLINE_CACHE
}

/**
 * State holder and coordinator for the AeroDex aircraft catalog, detail sheet, favorites, and comparator.
 *
 * Threading, Scope & Job management decisions:
 * - All coroutines run in [viewModelScope] (`SupervisorJob() + Dispatchers.Main.immediate`), so
 *   UI state updates are thread-safe and in-flight work is automatically cancelled on `onCleared()`.
 *   Heavy I/O, JSON, and mapping work is delegated to `Dispatchers.IO` inside the repository/data layers.
 * - Independent user flows use dedicated cancellable [Job] handles ([searchJob], [loadMoreJob],
 *   [detailJob], [compareJob]) implementing switch-map semantics: a new search cancels both
 *   [searchJob] and any stale [loadMoreJob]; a new detail selection cancels [detailJob].
 * - [loadComparisonDetails] uses `coroutineScope { async { ... } }` so parallel slot fetches
 *   cancel each other cleanly on failure without putting the parent [compareJob] into a cancelling state.
 * - [CancellationException] is always rethrown so structured concurrency is never broken.
 */
class AircraftViewModel(
    private val searchAircraftModelsUseCase: SearchAircraftModelsUseCase,
    private val getAircraftModelDetailUseCase: GetAircraftModelDetailUseCase,
    observeFavoriteCodesUseCase: ObserveFavoriteCodesUseCase,
    observeFavoritesUseCase: ObserveFavoritesUseCase,
    private val toggleFavoriteAircraftUseCase: ToggleFavoriteAircraftUseCase,
    private val debounceMs: Long = DEFAULT_DEBOUNCE_MS,
    autoLoadInitial: Boolean = true
) : ViewModel() {

    val uiState: StateFlow<AircraftUiState>
        field = MutableStateFlow(AircraftUiState())

    private var searchJob: Job? = null
    private var loadMoreJob: Job? = null
    private var detailJob: Job? = null
    private var compareJob: Job? = null
    private var activeSearchToken: Long = INITIAL_TOKEN
    private var activeDetailToken: Long = INITIAL_TOKEN

    init {
        combine(
            observeFavoriteCodesUseCase(),
            observeFavoritesUseCase()
        ) { codes, favs -> codes to favs }
            .onEach { (codes, favs) ->
                uiState.update { it.copy(favoriteCodes = codes, favorites = favs) }
            }
            .launchIn(viewModelScope)

        if (autoLoadInitial) {
            triggerSearch(immediate = true)
        }
    }

    fun selectTab(tab: AeroDexTab) {
        uiState.update {
            it.copy(
                selectedTab = tab,
                selectedIcaoCode = if (tab == AeroDexTab.COMPARATOR) null else it.selectedIcaoCode
            )
        }
        if (tab == AeroDexTab.COMPARATOR && uiState.value.compareCodes.isNotEmpty()) {
            loadComparisonDetails()
        }
    }

    fun onQueryChanged(newQuery: String) {
        if (uiState.value.query == newQuery) return
        uiState.update { it.copy(query = newQuery) }
        triggerSearch(immediate = false)
    }

    fun submitSearch() {
        triggerSearch(immediate = true)
    }

    fun clearQuery() {
        uiState.update { it.copy(query = "") }
        triggerSearch(immediate = true)
    }

    fun onCategorySelected(category: AircraftCategory?) {
        val nextCategory = if (uiState.value.selectedCategory == category) null else category
        uiState.update { it.copy(selectedCategory = nextCategory) }
        triggerSearch(immediate = true)
    }

    fun retrySearch() {
        triggerSearch(immediate = true)
    }

    fun loadMore() {
        val current = uiState.value
        val cursor = current.nextCursor
        if (!current.hasMore || cursor == null) return
        if (current.isLoading || current.isLoadingMore || loadMoreJob?.isActive == true) return

        val token = activeSearchToken
        loadMoreJob = viewModelScope.launch {
            uiState.update { it.copy(isLoadingMore = true) }
            try {
                val result = searchAircraftModelsUseCase(
                    query = current.query,
                    category = current.selectedCategory,
                    cursor = cursor
                )
                if (token != activeSearchToken) return@launch
                uiState.update { state ->
                    val merged = (state.items + result.items).distinctBy { it.icaoTypeCode }
                    state.copy(
                        items = merged,
                        nextCursor = result.nextCursor,
                        hasMore = result.hasMore,
                        searchOrigin = result.origin,
                        isLoadingMore = false
                    )
                }
            } catch (ce: CancellationException) {
                throw ce
            } catch (_: Throwable) {
                if (token != activeSearchToken) return@launch
                uiState.update { it.copy(isLoadingMore = false) }
            }
        }
    }

    fun selectAircraft(icaoTypeCode: String) {
        detailJob?.cancel()
        val token = ++activeDetailToken

        uiState.update {
            it.copy(
                selectedIcaoCode = icaoTypeCode,
                selectedDetail = null,
                detailOrigin = null,
                isDetailLoading = true,
                detailError = null
            )
        }

        detailJob = viewModelScope.launch {
            try {
                val result = getAircraftModelDetailUseCase(icaoTypeCode)
                if (token != activeDetailToken) return@launch
                uiState.update {
                    it.copy(
                        selectedDetail = result.detail,
                        detailOrigin = result.origin,
                        isDetailLoading = false,
                        detailError = null
                    )
                }
            } catch (ce: CancellationException) {
                throw ce
            } catch (error: Throwable) {
                if (token != activeDetailToken) return@launch
                uiState.update {
                    it.copy(
                        isDetailLoading = false,
                        detailError = error.message
                            ?.takeIf { msg -> msg.isNotBlank() }
                            ?.let(AircraftError::Message)
                            ?: AircraftError.DetailFailed
                    )
                }
            }
        }
    }

    fun retryDetail() {
        val code = uiState.value.selectedIcaoCode ?: return
        selectAircraft(code)
    }

    fun clearSelection() {
        detailJob?.cancel()
        activeDetailToken++
        uiState.update {
            it.copy(
                selectedIcaoCode = null,
                selectedDetail = null,
                detailOrigin = null,
                isDetailLoading = false,
                detailError = null
            )
        }
    }

    fun toggleFavorite(aircraft: AircraftModelSummary) {
        viewModelScope.launch {
            toggleFavoriteAircraftUseCase(aircraft)
        }
    }

    fun toggleFavoriteFromDetail(detail: AircraftModelDetail) {
        toggleFavorite(detail.toSummary())
    }

    fun toggleCompare(icaoTypeCode: String) {
        val normalized = icaoTypeCode.uppercase()
        val current = uiState.value.compareCodes
        val updated = when {
            normalized in current -> current - normalized
            current.size < MAX_COMPARE_ITEMS -> current + normalized
            else -> listOf(current.last(), normalized)
        }
        uiState.update { it.copy(compareCodes = updated) }
        loadComparisonDetails()
    }

    fun swapCompare() {
        val current = uiState.value.compareCodes
        if (current.size == MAX_COMPARE_ITEMS) {
            uiState.update {
                it.copy(
                    compareCodes = current.reversed(),
                    compareLeftDetail = it.compareRightDetail,
                    compareRightDetail = it.compareLeftDetail
                )
            }
        }
    }

    fun clearCompare() {
        compareJob?.cancel()
        uiState.update {
            it.copy(
                compareCodes = emptyList(),
                compareLeftDetail = null,
                compareRightDetail = null,
                isCompareLoading = false,
                compareError = null
            )
        }
    }

    private fun loadComparisonDetails() {
        val state = uiState.value
        val codes = state.compareCodes
        if (codes.isEmpty()) {
            compareJob?.cancel()
            uiState.update {
                it.copy(
                    compareLeftDetail = null,
                    compareRightDetail = null,
                    isCompareLoading = false,
                    compareError = null
                )
            }
            return
        }

        val cachedDetails = listOfNotNull(
            state.compareLeftDetail,
            state.compareRightDetail,
            state.selectedDetail
        ).associateBy { it.icaoTypeCode.uppercase() }

        val leftCode = codes[0].uppercase()
        val rightCode = codes.getOrNull(1)?.uppercase()
        val cachedLeft = cachedDetails[leftCode]
        val cachedRight = rightCode?.let { cachedDetails[it] }

        if (cachedLeft != null && (rightCode == null || cachedRight != null)) {
            compareJob?.cancel()
            uiState.update {
                it.copy(
                    compareLeftDetail = cachedLeft,
                    compareRightDetail = cachedRight,
                    isCompareLoading = false,
                    compareError = null
                )
            }
            return
        }

        compareJob?.cancel()
        compareJob = viewModelScope.launch {
            uiState.update {
                it.copy(
                    compareLeftDetail = cachedLeft,
                    compareRightDetail = cachedRight,
                    isCompareLoading = true,
                    compareError = null
                )
            }
            try {
                val (left, right) = coroutineScope {
                    val leftDeferred = if (cachedLeft == null) {
                        async { getAircraftModelDetailUseCase(leftCode).detail }
                    } else null
                    val rightDeferred = if (rightCode != null && cachedRight == null) {
                        async { getAircraftModelDetailUseCase(rightCode).detail }
                    } else null
                    (cachedLeft ?: leftDeferred?.await()) to (cachedRight ?: rightDeferred?.await())
                }
                uiState.update {
                    it.copy(
                        compareLeftDetail = left,
                        compareRightDetail = right,
                        isCompareLoading = false
                    )
                }
            } catch (ce: CancellationException) {
                throw ce
            } catch (error: Throwable) {
                uiState.update {
                    it.copy(
                        isCompareLoading = false,
                        compareError = error.message
                            ?.takeIf { msg -> msg.isNotBlank() }
                            ?.let(AircraftError::Message)
                            ?: AircraftError.CompareFailed
                    )
                }
            }
        }
    }

    private fun triggerSearch(immediate: Boolean) {
        searchJob?.cancel()
        loadMoreJob?.cancel()
        val token = ++activeSearchToken
        val querySnapshot = uiState.value.query
        val categorySnapshot = uiState.value.selectedCategory
        val shouldDebounce = !immediate && debounceMs > NO_DEBOUNCE_MS

        if (!shouldDebounce) {
            uiState.update {
                it.copy(
                    isLoading = true,
                    isLoadingMore = false,
                    searchError = null
                )
            }
        }

        searchJob = viewModelScope.launch {
            if (shouldDebounce) {
                delay(debounceMs)
                uiState.update {
                    it.copy(
                        isLoading = true,
                        isLoadingMore = false,
                        searchError = null
                    )
                }
            }
            try {
                val result = searchAircraftModelsUseCase(
                    query = querySnapshot,
                    category = categorySnapshot,
                    cursor = null
                )
                if (token != activeSearchToken) return@launch
                uiState.update {
                    it.copy(
                        items = result.items,
                        nextCursor = result.nextCursor,
                        hasMore = result.hasMore,
                        searchOrigin = result.origin,
                        isLoading = false,
                        searchError = null
                    )
                }
            } catch (ce: CancellationException) {
                throw ce
            } catch (error: Throwable) {
                if (token != activeSearchToken) return@launch
                uiState.update {
                    it.copy(
                        isLoading = false,
                        searchError = error.message
                            ?.takeIf { msg -> msg.isNotBlank() }
                            ?.let(AircraftError::Message)
                            ?: AircraftError.SearchFailed
                    )
                }
            }
        }
    }

    private fun AircraftModelDetail.toSummary(): AircraftModelSummary = AircraftModelSummary(
        icaoTypeCode = icaoTypeCode,
        iataTypeCode = iataTypeCode,
        manufacturer = manufacturer,
        model = model,
        name = name,
        category = category,
        photo = photos.firstOrNull(),
        firstFlightYear = firstFlightYear,
        engineCount = propulsion?.engineCount,
        engineType = propulsion?.engineType,
        wakeTurbulenceCategory = wakeTurbulenceCategory,
        maxPassengers = capacity?.maxPassengers,
        cruiseSpeedKt = performance?.cruiseSpeedKt,
        maxSpeedKt = performance?.maxSpeedKt,
        rangeNm = performance?.rangeNm,
        rangeKm = performance?.rangeKm
    )

    companion object {
        const val DEFAULT_DEBOUNCE_MS = 300L
        const val NO_DEBOUNCE_MS = 0L
        const val INITIAL_TOKEN = 0L
        const val MAX_COMPARE_ITEMS = 2
    }
}
