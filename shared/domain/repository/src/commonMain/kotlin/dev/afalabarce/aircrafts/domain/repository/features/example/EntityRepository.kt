package dev.afalabarce.aircrafts.domain.repository.features.example

import dev.afalabarce.aircrafts.domain.models.ExampleEntity
import kotlinx.coroutines.flow.Flow

interface EntityRepository {
    fun getEntities(): Flow<List<ExampleEntity>>
}
