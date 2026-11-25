package dev.afalabarce.aircrafts.data.datasources.features.example

import dev.afalabarce.aircrafts.models.features.example.local.CacheExampleEntity
import kotlinx.coroutines.flow.Flow

interface EntityDataStore {
    fun getEntities(): Flow<List<CacheExampleEntity>>
}
