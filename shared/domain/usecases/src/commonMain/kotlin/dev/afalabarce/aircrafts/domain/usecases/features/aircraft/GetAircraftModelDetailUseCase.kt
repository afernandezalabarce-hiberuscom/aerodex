package dev.afalabarce.aircrafts.domain.usecases.features.aircraft

import dev.afalabarce.aircrafts.domain.models.AircraftDetailResult
import dev.afalabarce.aircrafts.domain.repository.features.aircraft.AircraftRepository

/**
 * Fetches the full technical sheet for a specific aircraft model by its ICAO type code.
 */
class GetAircraftModelDetailUseCase(
    private val repository: AircraftRepository
) {
    suspend operator fun invoke(icaoTypeCode: String): AircraftDetailResult {
        val normalizedCode = icaoTypeCode.trim().uppercase()
        require(normalizedCode.isNotEmpty()) { "ICAO type code must not be blank" }
        return repository.getAircraftModelDetail(normalizedCode)
    }
}
