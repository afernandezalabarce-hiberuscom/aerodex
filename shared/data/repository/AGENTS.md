# Module `:shared:data:repository`

## Overview
Implements domain repository contracts (`:shared:domain:repository`). Orchestrates data: picks source (local/remote), transforms to domain models.

## Responsibilities
- Implement `domain` interfaces.
- Orchestrate data access (e.g. Single Source of Truth).
- Map `data:models` ↔ `domain:models`.
- Hide datasource complexity from domain.
- No Android/iOS-specific logic.

## Structure
```text
dev.afalabarce.template.kmp.data.repository.features
└── <feature_name>
    ├── <FeatureName>RepositoryImpl.kt
    └── mappers
        └── <FeatureName>Mappers.kt
```

## Mappers
All mapping via extension functions in each feature's `mappers` package (imperative).
- File: `<FeatureName>Mappers.kt`.
- Form: extension functions.
- Naming:
    - `toDomain()`: data → domain.
    - `toCache()` / `toEntity()`: domain/remote → local persistence.
    - `toRemote()` / `toDto()`: domain → network.

```kotlin
// File: ExampleMappers.kt
fun CacheSimpleEntity.toDomain(): ExampleEntity = ExampleEntity(
    id = id,
    title = title,
    description = description
)

fun ExampleEntity.toCache(): CacheSimpleEntity = CacheSimpleEntity(
    id = id,
    title = title,
    description = description
)
```

## Repository impl
Inject the feature's datasource interfaces from `:shared:data:datasources`.
- Inject via primary constructor.
- Named args, one per line.
- Map with the extension functions.

```kotlin
class ExampleRepositoryImpl(
    private val localDataSource: ExampleLocalDataSource,
    private val remoteDataSource: ExampleRemoteDataSource,
) : ExampleRepository {

    override fun getItems(): Flow<List<ExampleEntity>> {
        return localDataSource.getAll().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun syncItems() {
        val remoteData = remoteDataSource.fetchItems()
        localDataSource.saveItems(
            items = remoteData.map { it.toCache() }
        )
    }
}
```

## DI (Koin)
Register each repository in `DataRepositoryDependencyInjector` (implements `KoinModuleLoader`) with `factoryOf(::Impl) bind Interface::class`.

```kotlin
object DataRepositoryDependencyInjector : KoinModuleLoader {
    override val modules: List<Module>
        get() = listOf(
            module {
                factoryOf(::ExampleRepositoryImpl) bind ExampleRepository::class
            }
        )
}
```

## Golden rules
1. NEVER map inline in repository functions; always use `mappers` extension functions.
2. Repository only knows `datasources` interfaces + `data`/`domain` models.
3. Keep mapper files organized and atomic per feature.
4. Named args + line breaks for readability.
5. **NO MAGIC VALUES**: Never hardcode magic strings (error messages, provider names, device identifiers) or magic numbers; use named constants or typed domain errors (`sealed interface` / `sealed object`).

## Testing
All tests live in `src/commonTest/kotlin/` using `kotlin.test` and `kotlinx.coroutines.test.runTest`. Never use reflection-based mocking frameworks (Mockito/MockK).

- **Unit Tests**:
  - **Mappers** (`<FeatureName>MappersTest`): verify `toDomain()`, `toCache()`, `toEntity()`, and `toRemote()`/`toDto()` field mappings, nullability, and default fallbacks.
  - **Cache** (`<FeatureName>CacheTest`): verify TTL expiration, eviction, key normalization, and invalidation.
- **Integration Tests** (`<FeatureName>RepositoryIntegrationTest`):
  - Wire the real `<FeatureName>RepositoryImpl` with the real `<FeatureName>RemoteDataSourceImpl` (backed by Ktor `MockEngine` + `Ktorfit` + `Json`) and an in-memory **Fake** `<FeatureName>LocalDataSource` (plus real in-memory cache if applicable).
  - Verify Single Source of Truth / offline-first behavior, cache hits vs. network fetches, fallback to local cache on network failure, `Flow` emissions, and `Result` error mapping.
- **Execution**:
  - `./gradlew :shared:data:repository:testAndroidHostTest`
  - `./gradlew :shared:data:repository:iosSimulatorArm64Test`
