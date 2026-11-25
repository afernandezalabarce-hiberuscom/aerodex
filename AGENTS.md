# KMMTemplate (AeroDex) - Agent Guide

KMMTemplate = Kotlin Multiplatform project (base package `dev.afalabarce.aircrafts`), Android + iOS, Compose Multiplatform, strict Clean Architecture inside `shared`.

## Modules

### Core (`:shared:core:*`)
- `core:common`: cross-cutting utils, base extensions, `KoinModuleLoader`. Visible to all. See [core:common/AGENTS.md](shared/core/common/AGENTS.md).
- `core:viewmodels`: base `KmpViewModel`, `safeLaunch`.
- `core:di`: central dependency tree.
- `core:ui`: `AppMaterialTheme`, `MobileDimens`, Compose wrappers. See [core:ui/AGENTS.md](shared/core/ui/AGENTS.md).

### Data (`:shared:data:*`)
- `data:models`: DTOs + Room `@Entity`. See [data:models/AGENTS.md](shared/data/models/AGENTS.md).
- `data:datasources`: data access contracts (local + remote). See [data:datasources/AGENTS.md](shared/data/datasources/AGENTS.md).
- `data:datasources-core`: real implementations. Room (DB), Ktor + Ktorfit (REST), DataStore (prefs). See [data:datasources-core/AGENTS.md](shared/data/datasources-core/AGENTS.md).
- `data:repository`: repo implementations + data→domain mapping. See [data:repository/AGENTS.md](shared/data/repository/AGENTS.md).

### Domain (`:shared:domain:*`)
- `domain:models`: pure business models (POJOs). See [domain:models/AGENTS.md](shared/domain/models/AGENTS.md).
- `domain:repository`: repo contracts. See [domain:repository/AGENTS.md](shared/domain/repository/AGENTS.md).
- `domain:usecases`: atomic use cases (`operator fun invoke`). See [domain:usecases/AGENTS.md](shared/domain/usecases/AGENTS.md).

### Presentation (`:shared:presentation:*`)
- `presentation:viewModels`: UI state. Extend `KmpViewModel`. See [presentation:viewModels/AGENTS.md](shared/presentation/viewModels/AGENTS.md).
- `presentation:ui`: screens + Compose UI. See [presentation:ui/AGENTS.md](shared/presentation/ui/AGENTS.md).

### Native + build
- `androidApp`: Android entry (`MainActivity`).
- `iosApp`: iOS entry (SwiftUI wrapper calling `App()`).
- `buildSrc/BuildVersion.kt`: SDK versions, app IDs, dependency versions.

## Compose resources & Localization (MANDATORY)
- Location: `shared/presentation/ui/src/commonMain/composeResources` (`values/strings.xml`, `values-es/strings.xml`, etc.).
- Generates `Res` class (Compose Multiplatform Resources). Always use `stringResource(Res.string.key_name)`.
- **NO HARDCODED UI STRINGS**: Every user-visible text in the application MUST be defined in `composeResources/values*/strings.xml`.
- **NO STRINGS FROM VIEWMODELS**: ViewModels (`:shared:presentation:viewmodels`) are strictly forbidden from emitting raw display strings (`String`) for errors, statuses, or labels. Instead, a ViewModel MUST emit a typed state/reason using a `sealed interface` / `sealed class` with `data object` (or `data class` for parameters), which the UI layer (`:shared:presentation:ui`) captures and maps to `stringResource(Res.string.key_name)`.

## DI (Koin)
Each module that injects:
1. Make object implementing `KoinModuleLoader` in `.di`.
2. Define its Koin modules.
3. Platform deps: use `expect fun getPlatformInjects(): List<Module>`.

DI-bearing modules: `data:datasources-core`, `data:repository`, `domain:usecases`, `presentation:viewModels`.

## New feature flow
1. Domain: models → repository contract → use cases.
2. Data: data models → datasources → repository impl (with mapping).
3. Presentation: ViewModel (uses use cases) → UI (collects ViewModel state).
4. DI: register every new dependency in its Koin module.

## Expect/Actual
- Declare `expect` in `commonMain`.
- Implement `actual` in `androidMain` + `iosMain`.
- File names: `Class.android.kt`, `Class.ios.kt`.

## Offline-first
- Room = ORM.
- New `data:models` model goes in its feature package.
- Migrations live in `AppDatabase` (`data:datasources-core`).
- Prefer auto migrations; manual SQL only when strictly needed.
- DAO inserts use `@Upsert`.
- CRUD inserts/updates/deletes run in a transaction:
```kotlin
database.useWriterConnection { transactor ->
  transactor.withTransaction(type = Transactor.SQLiteTransactionType.IMMEDIATE) {
    // DB calls
  }
  // reached here = transaction OK, else exception is thrown and must be handled
  true
}
```

## Mappers
- Live in `data:repository` (only layer seeing both `data` + `domain`).
- Extension functions, file `<FeatureName>Mappers.kt`, in `mappers` package per feature.

## Code conventions
- English for code, names, technical comments.
- **CRITICAL NETWORK RESTRICTION**: The ONLY valid network client is `ktor-client` + `ktorfit` using the engines configured in `gradle/libs.versions.toml` (`ktor-client-okhttp` / `ktor-client-android` on Android, `ktor-client-darwin` / `ktor-client-ios` on iOS). Third-party HTTP clients outside Ktor/Ktorfit (such as `Retrofit`, standalone `OkHttp`, or raw `NSURLSession` wrappers) are strictly forbidden.
- **STRICT PROHIBITION OF MAGIC VALUES**: Creating magic values is strictly forbidden across the entire project. This applies to strings (must use `composeResources/values*/strings.xml` for UI or named constants for technical keys/endpoints), dimensions/paddings/sizes (must use `MobileDimens` / `AppMaterialTheme.dimens`), colors (must use `AppMaterialTheme.colorScheme` / theme tokens in `Color.kt`), and numeric thresholds/timeouts (must use named constants).
- **TYPED VIEWMODEL STATES FOR TEXT**: ViewModels must never emit raw UI strings; emit `sealed interface` / `data object` state markers and map them in `:shared:presentation:ui` via `stringResource(Res.string.*)`.
- ViewModels: use `safeLaunch` for async (unified loading/error).
- Repositories map `data` → `domain`.
- Named args, each on its own line:
```kotlin
exampleFunction(
    paramA = v1,
    paramB = v2,
)
```
- `presentation:ui` collects StateFlow with `collectAsStateWithLifecycle()`, not `collectAsState()`.

## Antigravity Brain, Scratch & Task Management (MANDATORY & PRE-APPROVED)
- **Transparent Execution Without Interactive Prompts**: All operations within `<appDataDir>/brain/` (including reading/writing artifacts, inspecting transcripts, generating scratch scripts, running helper utilities, reading task outputs, and updating local specs under `specs`) are permanently authorized and pre-approved.
- **Autonomous Task Management**: Agents MUST NOT ask the user for interactive confirmations, manual authorizations, or elevated permissions for internal task lifecycle actions, brain file updates, build checks, or routine scratch scripts.
- **Strict Reservation of User Confirmations**: User permissions are strictly reserved for critical, irreversible, or high-impact actions outside the local workspace and project sandbox (such as destructive system file deletions, external OS configuration modifications, or sensitive third-party credential resets). All routine task management and workflow operations must run autonomously and seamlessly.

## Testing Strategy & Tooling
Tests live primarily in `commonTest/` (shared across Android and iOS) using `kotlin.test` and `kotlinx.coroutines.test`. Android host-specific runners/resources live in `androidHostTest/`, and iOS-specific tests live in `iosTest/`.

### Test Pyramid by Layer
- **Unit Tests**:
  - `core:common`: pure utility and extension tests.
  - `data:repository`: mapper (`toDomain`/`toCache`/`toRemote`) and memory-cache unit tests.
  - `domain:usecases`: business rule, input validation/normalization, and delegation tests using in-memory **Fake** repositories.
  - `presentation:viewModels`: `StateFlow`/`SharedFlow`, debounce, retry, and error-state tests using `StandardTestDispatcher` (`Dispatchers.setMain`/`resetMain`) and Fake repositories.
  - `core:ui` & `presentation:ui`: isolated Compose component tests (`runComposeUiTest`) verifying rendering, callbacks, and WCAG 2.2 semantics.
- **Integration Tests**:
  - `data:datasources-core`: Room DAO/transaction tests (in-memory DB via `MultiplatformDatabaseTest` / Robolectric) and Ktorfit `RemoteDataSourceImpl` tests with Ktor `MockEngine`.
  - `data:repository`: `RepositoryImpl` + real `RemoteDataSourceImpl` (Ktor `MockEngine` + Ktorfit + `Json`) + in-memory `LocalDataSource` + real cache.
- **UI End-to-End (E2E) Tests**:
  - `presentation:ui`: full-flow `runComposeUiTest` + `@CommonRunWith(PlatformTestRunner::class)` (Robolectric on `androidHostTest`, native on `iosTest`), wiring `Screen` + navigation + real `ViewModel` + real `UseCases` + real `RepositoryImpl` + `RemoteDataSourceImpl` (`MockEngine`) + in-memory `LocalDataSource`.

### Mandatory Testing Rules
- **No Mockito / MockK**: Do not introduce reflection-based mocking libraries; use in-memory **Fakes** for interfaces and `io.ktor:ktor-client-mock` (`MockEngine`) for HTTP responses.
- **No Real Network or Production DB**: All tests must be deterministic and offline-safe (`MockEngine` + in-memory Room/fakes).
- **Android Host Tests**: Modules running JVM host tests must enable `withHostTest {}` (and `isIncludeAndroidResources = true` for Robolectric/Compose UI) in `kotlin { android { ... } }` inside `build.gradle.kts`.
- **Contract / Pure Model Modules**: `data:models`, `data:datasources`, `domain:models`, and `domain:repository` do not require dedicated test suites unless a domain model includes non-trivial validation logic.

## Useful Gradle tasks
- `./gradlew :shared:presentation:ui:compileAndroidMain` — compile UI for Android (check generated resources).
- `./gradlew :shared:<module>:testAndroidHostTest` — run `commonTest` + `androidHostTest` on JVM (requires `withHostTest {}` in module `build.gradle.kts`).
- `./gradlew :shared:<module>:iosSimulatorArm64Test` — run `commonTest` + `iosTest` on iOS Simulator.
- `./gradlew initializeKmpProject --appName="..." --destinationPackage="..."` — init new project from template.
