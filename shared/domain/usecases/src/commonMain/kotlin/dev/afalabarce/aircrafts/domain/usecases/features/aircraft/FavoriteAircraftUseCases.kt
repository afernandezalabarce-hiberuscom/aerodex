package dev.afalabarce.aircrafts.domain.usecases.features.aircraft

import dev.afalabarce.aircrafts.domain.models.AircraftModelSummary
import dev.afalabarce.aircrafts.domain.repository.features.aircraft.AircraftRepository
import kotlinx.coroutines.flow.Flow

class ObserveFavoriteCodesUseCase(
    private val repository: AircraftRepository
) {
    operator fun invoke(): Flow<Set<String>> = repository.observeFavoriteCodes()
}

class ObserveFavoritesUseCase(
    private val repository: AircraftRepository
) {
    operator fun invoke(): Flow<List<AircraftModelSummary>> = repository.observeFavorites()
}

class ToggleFavoriteAircraftUseCase(
    private val repository: AircraftRepository
) {
    suspend operator fun invoke(aircraft: AircraftModelSummary): Boolean =
        repository.toggleFavorite(aircraft)
}
