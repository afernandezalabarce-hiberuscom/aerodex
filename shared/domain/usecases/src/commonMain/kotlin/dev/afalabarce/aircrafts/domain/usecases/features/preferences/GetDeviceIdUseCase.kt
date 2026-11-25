package dev.afalabarce.aircrafts.domain.usecases.features.preferences

import dev.afalabarce.aircrafts.domain.repository.features.preferences.PreferencesRepository
import kotlinx.coroutines.flow.Flow

class GetDeviceIdUseCase(private val repository: PreferencesRepository) {
    operator fun invoke(): Flow<Long> = this.repository.getDeviceId()
}
