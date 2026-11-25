package dev.afalabarce.aircrafts.domain.usecases.features.example

import dev.afalabarce.aircrafts.domain.models.ExampleEntity
import dev.afalabarce.aircrafts.domain.repository.features.example.EntityRepository
import kotlinx.coroutines.flow.Flow

class GetEntitiesUseCase(private val repository: EntityRepository) {
    operator fun invoke(): Flow<List<ExampleEntity>> = repository.getEntities()
}
