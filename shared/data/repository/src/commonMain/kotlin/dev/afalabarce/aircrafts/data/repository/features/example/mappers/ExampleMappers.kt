package dev.afalabarce.aircrafts.data.repository.features.example.mappers

import dev.afalabarce.aircrafts.models.features.example.local.CacheExampleEntity
import dev.afalabarce.aircrafts.models.features.example.remote.RemoteExampleEntity
import dev.afalabarce.aircrafts.domain.models.ExampleEntity

fun ExampleEntity.toCache(): CacheExampleEntity = CacheExampleEntity(
    id = id,
    title = title,
    description = description
)

fun ExampleEntity.toRemote(): RemoteExampleEntity = RemoteExampleEntity(
    remoteId = id,
    remoteTitle = title,
    remoteDescription = description
)

fun RemoteExampleEntity.toCache(): CacheExampleEntity = CacheExampleEntity(
    id = remoteId,
    title = remoteTitle,
    description = remoteDescription
)

fun CacheExampleEntity.toDomain(): ExampleEntity = ExampleEntity(
    id = id,
    title = title,
    description = description
)
