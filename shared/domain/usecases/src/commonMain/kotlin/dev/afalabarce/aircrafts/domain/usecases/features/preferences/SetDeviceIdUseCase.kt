package dev.afalabarce.aircrafts.domain.usecases.features.preferences

import dev.afalabarce.aircrafts.domain.repository.features.preferences.PreferencesRepository

class SetDeviceIdUseCase(private val repository: PreferencesRepository) {
    suspend operator fun invoke(deviceId: Long) = this.repository.setDeviceId(deviceId)
}
