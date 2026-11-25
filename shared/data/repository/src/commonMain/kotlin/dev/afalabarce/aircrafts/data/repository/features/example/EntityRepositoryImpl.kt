package dev.afalabarce.aircrafts.data.repository.features.example

import dev.afalabarce.aircrafts.data.datasources.features.example.EntityDataStore
import dev.afalabarce.aircrafts.domain.models.ExampleEntity
import dev.afalabarce.aircrafts.domain.repository.features.example.EntityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EntityRepositoryImpl (private val dataSource: EntityDataStore): EntityRepository {
    override fun getEntities(): Flow<List<ExampleEntity>> = dataSource.getEntities().map { data ->
        data.map { entity -> ExampleEntity(
            id = entity.id,
            title = entity.title,
            description = entity.description
        ) }
    }

}
