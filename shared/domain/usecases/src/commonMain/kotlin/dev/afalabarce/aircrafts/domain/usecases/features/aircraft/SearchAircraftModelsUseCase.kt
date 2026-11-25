package dev.afalabarce.aircrafts.domain.usecases.features.aircraft

import dev.afalabarce.aircrafts.domain.models.AircraftCategory
import dev.afalabarce.aircrafts.domain.models.AircraftSearchResult
import dev.afalabarce.aircrafts.domain.repository.features.aircraft.AircraftRepository

/**
 * Searches aircraft models by free-text query and optional category filter.
 */
class SearchAircraftModelsUseCase(
    private val repository: AircraftRepository
) {
    suspend operator fun invoke(
        query: String,
        category: AircraftCategory? = null,
        cursor: String? = null,
        limit: Int = DEFAULT_PAGE_SIZE
    ): AircraftSearchResult {
        val normalizedQuery = query.trim()
        return repository.searchAircraftModels(
            query = normalizedQuery,
            category = category,
            cursor = cursor,
            limit = limit
        )
    }

    companion object {
        const val DEFAULT_PAGE_SIZE = 20
    }
}
