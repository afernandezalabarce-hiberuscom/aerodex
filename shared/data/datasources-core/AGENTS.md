# Module `:shared:data:datasources-core`

## Role
Expert KMP dev. Real datasource implementations. Strong in:
- DB design with [Room](https://developer.android.com/training/data-storage/room) + SQL optimization.
    - Deep [SQLite](https://sqlite.org/docs.html) knowledge, referential integrity, cascades.
    - Table redesign, SQL views, and Code-First Room migrations from `data:models` entities. Prefer auto migrations; use manual SQL when required (type changes, derived fields...).
- REST with ktorfit; map each endpoint response to a controlled error by HTTP status.
- Error handling in datasource impls (from `data:datasources`), returning `Flow` of `Result` when possible. Keep errors in the innermost layer.

## Structure
```text
data:datasources-core/
├── build.gradle.kts
├── AGENTS.md
└── src/
    ├── commonMain/
    │   └── kotlin/io/afalabarce/template/kmm/data/datasources/core/
    │       ├── db/
    │       │   └── AppDatabase.kt                          # DB def, migrations, DAO links
    │       ├── di/
    │       │   └── DataSourceCoreDependencyInjector.kt     # Koin loader
    │       ├── features/
    │       │   ├── <FeatureNameA>/
    │       │   │   ├── dao/
    │       │   │   │   └── <FeatureNameA>Dao.kt
    │       │   │   ├── <FeatureName>DataStoreImpl.kt
    │       │   │   └── <FeatureNameA>RemoteDataSourceImpl.kt
    │       │   └── <FeatureNameZ>/
    │       │       ├── dao/
    │       │       │   └── <FeatureNameZ>Dao.kt
    │       │       ├── <FeatureNameZ>DataStoreImpl.kt
    │       │       └── <FeatureNameZ>RemoteDataSourceImpl.kt
    │       └── remote/
    │           └── ApiService.kt                           # global API interface
    ├── androidMain/                                        # actual impls for Android (OPTIONAL)
    │   └── kotlin/io/afalabarce/template/kmm/data/datasources/core/
    │       ├── db/
    │       ├── di/
    │       │   └── DataSourceCoreDependencyInjector.android.kt
    │       └── features/
    │           ├── <FeatureNameA>/
    │           │   ├── dao/
    │           │   │   └── <FeatureNameA>Dao.android.kt
    │           │   ├── <FeatureName>DataStoreImpl.android.kt
    │           │   └── <FeatureNameA>RemoteDataSourceImpl.android.kt
    │           └── <FeatureNameZ>/
    │               ├── dao/
    │               │   └── <FeatureNameZ>Dao.android.kt
    │               ├── <FeatureNameZ>DataStoreImpl.android.kt
    │               └── <FeatureNameZ>RemoteDataSourceImpl.android.kt
    └── iosMain/                                            # actual impls for iOS (OPTIONAL)
        └── kotlin/io/afalabarce/template/kmm/data/datasources/core/
            ├── db/
            ├── di/
            │   └── DataSourceCoreDependencyInjector.ios.kt
            └── features/
                ├── <FeatureNameA>/
                │   ├── dao/
                │   │   └── <FeatureNameA>Dao.ios.kt
                │   ├── <FeatureName>DataStoreImpl.ios.kt
                │   └── <FeatureNameA>RemoteDataSourceImpl.ios.kt
                └── <FeatureNameZ>/
                    ├── dao/
                    │   └── <FeatureNameZ>Dao.ios.kt
                    ├── <FeatureNameZ>DataStoreImpl.ios.kt
                    └── <FeatureNameZ>RemoteDataSourceImpl.ios.kt
```

## Rules
- English for names + comments. Always trailing commas.
- **NO MAGIC VALUES**: Never hardcode magic strings (routes, headers, keys, table names) or magic numbers (timeouts, status codes, versions) inline; extract named constants.
- DI: watch injection order to avoid runtime errors.
- DI: prefer Koin DSL (`singleOf`, `factoryOf`...). Use classic versions only for custom `named` injects.

```kotlin
object DataSourceCoreDependencyInjector : KoinModuleLoader {
  override val modules: List<Module>
    get() = getPlatformInjects().union(
      listOf(
        module {
            singleOf(::AppDatabase)
            factoryOf(::<FeatureNameA>LocalDatasourceImpl) bind <FeatureNameA>LocalDatasource::class
            factoryOf(::<FeatureNameA>RemoteDatasourceImpl) bind <FeatureNameA>RemoteDatasource::class
            factoryOf(::<FeatureNameZ>LocalDatasourceImpl) bind <FeatureNameZ>LocalDatasource::class
            factoryOf(::<FeatureNameZ>RemoteDatasourceImpl) bind <FeatureNameZ>RemoteDatasource::class
            factory(named("customNameInject")) {
                <FeatureNameX>LocalDatasource(get(), get())
            }
            factory {
              <FeatureNameY>LocalDatasource(get(named("customNameInject")), get())
            }
        })
      )
```

- When required, declare `expect` in `commonMain` + `actual` per platform.

### DAOs
- Insert/delete functions MUST be `suspend` (Room forbids them on main thread).
- Read functions return `Flow`; if a read must not be a `Flow`, mark it `suspend`.
- Inserts annotated `@Upsert`. Avoid `@Insert` (prevents accidental integrity-related deletes).
- CRUD inserts/deletes MUST run in a transaction:
```kotlin
database.useWriterConnection { transactor ->
  transactor.withTransaction(type = Transactor.SQLiteTransactionType.IMMEDIATE) {
    // DB calls
  }
  // reached here = transaction OK, else exception thrown and must be handled
  true
}
```

### Remote (ktor-client / ktorfit)
- **CRITICAL NETWORK RESTRICTION**: The ONLY valid network client is `ktor-client` + `ktorfit` using the engines configured in `gradle/libs.versions.toml` (`ktor-client-okhttp` / `ktor-client-android` on Android, `ktor-client-darwin` / `ktor-client-ios` on iOS). Third-party HTTP clients outside Ktor/Ktorfit (such as `Retrofit`, standalone `OkHttp`, etc.) are strictly forbidden.
- All functions `suspend`, except those returning a `Flow`.
- Return the required type directly unless more logic is needed.
- For advanced HTTP-state handling, return `Response<T>`.

### Error handling (innermost layer)
- `data:datasources` impls live here; everything returns a `Result` (flow-wrapped or not).
    - Room: handle errors with `try-catch-finally`, emit the matching `Result`.
    - ktorfit:
        - Common type: handle errors with specific `try-catch-finally`; enumerate all possible exceptions.
        - `Response<T>`: map to `Result` by Response `statusCode`.

## Testing
- **Remote Datasource (Unit & Integration) Tests** in `src/commonTest/kotlin/`:
    - Use `io.ktor:ktor-client-mock` (`MockEngine`) + `ContentNegotiation` (`Json`) + `Ktorfit` (`createApiService()`) + `kotlinx.coroutines.test.runTest`.
    - Test request paths/query params, DTO deserialization, HTTP error status mapping (4xx/5xx), and network exception handling without real network calls.
- **Local Room / DAO (Integration) Tests**:
    - Use an in-memory `RoomDatabase` via `MultiplatformDatabaseTest` (`commonTest` with `androidUnitTest`/`androidHostTest` Robolectric implementation and `iosTest` native SQLite implementation).
    - Verify `@Upsert` idempotency, queries, `Transactor` `IMMEDIATE` transactions, foreign keys, and cascade deletes.
- **Execution**: Enable `withHostTest {}` in `build.gradle.kts` under `kotlin.android` when running JVM host tests:
    - `./gradlew :shared:data:datasources-core:testAndroidHostTest`
    - `./gradlew :shared:data:datasources-core:iosSimulatorArm64Test`
