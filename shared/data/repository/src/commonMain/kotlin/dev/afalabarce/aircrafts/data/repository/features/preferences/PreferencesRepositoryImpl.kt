package dev.afalabarce.aircrafts.data.repository.features.preferences

import dev.afalabarce.aircrafts.data.datasources.features.preferences.AppPreferences
import dev.afalabarce.aircrafts.domain.repository.features.preferences.PreferencesRepository
import kotlinx.coroutines.flow.Flow

class PreferencesRepositoryImpl(private val preferences: AppPreferences) : PreferencesRepository {
    override fun getDeviceId(): Flow<Long> = this.preferences.getDeviceId()

    override suspend fun setDeviceId(deviceId: Long) = this.preferences.setDeviceId(deviceId)

}
