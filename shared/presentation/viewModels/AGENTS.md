# Module `:shared:presentation:viewModels`

## Overview
Manages UI state; orchestrates between presentation and domain use cases. Holds ViewModels exposing observable state to the UI.

## Responsibilities
- Expose UI state flows (`StateFlow`, `SharedFlow`).
- Orchestrate use cases to act and update state.
- Use ViewModel lifecycle to manage resources + cancel coroutines.

## Platform constraints
- Pure Kotlin in `commonMain`.
- No business logic (lives in domain).
- No specific UI deps (`ComponentActivity`, `Context`).

## Structure (feature-based)
```text
dev.afalabarce.template.kmp.presentation.viewmodels.features
└── <feature_name>
    ├── uistates
    │   └── <FeatureName>UiState.kt
    └── <FeatureName>ViewModel.kt
```

## Design + state
- Extend `KmpViewModel` (from `:shared:core:viewmodels`); gives `safeLaunch`.
- Expose state via `StateFlow` (always has an initial value).
- Share state across collectors with `shareIn` + `viewModelScope` + `SharingStarted.Lazily`.
- State is immutable `data class`.
- Complex state: `sealed class` (Loading, Success, Error, Empty...) in the feature `uistates` package.
- Use [Explicit Backing Fields](https://kotlinlang.org/docs/whatsnew23.html#explicit-backing-fields)
as possible (stable in Kotlin 2.4.20)

```kotlin
package dev.afalabarce.template.kmp.presentation.viewmodels.features.example

import androidx.lifecycle.viewModelScope
import dev.afalabarce.template.kmp.core.viewmodels.KmpViewModel
import dev.afalabarce.template.kmp.domain.usecases.features.example.GetEntitiesUseCase
import dev.afalabarce.template.kmp.domain.models.ExampleEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class ExampleUiState {
    object Loading : ExampleUiState()
    data class Success(val entities: List<ExampleEntity>) : ExampleUiState()
    data class Error(val message: String) : ExampleUiState()
}

class ExampleViewModel(
    private val getEntitiesUseCase: GetEntitiesUseCase,
) : KmpViewModel() {

    private val _uiState = MutableStateFlow<ExampleUiState>(ExampleUiState.Loading)
    val uiState: StateFlow<ExampleUiState> = _uiState

    fun loadEntities() {
        viewModelScope.launch {
            getEntitiesUseCase()
                .map { result ->
                    result.onSuccess { entities ->
                        _uiState.value = ExampleUiState.Success(entities)
                    }.onFailure { throwable ->
                        _uiState.value = ExampleUiState.Error(throwable.message ?: "Unknown error")
                    }
                }
                .collect { /* Handle flow */ }
        }
    }

    fun onRetry() {
        viewModelScope.launch {
            _uiState.value = ExampleUiState.Loading
            loadEntities()
        }
    }
}
```

## safeLaunch
For async work needing unified loading/error handling:
```kotlin
fun loadData() {
    safeLaunch(
        onStart = { /* show loading */ },
        onFinish = { /* hide loading */ },
        onError = { error -> /* show error */ },
        launchBody = {
            val result = useCase()
            // update state from result
        }
    )
}
```

## DI (Koin)
Register each ViewModel in `PresentationViewModelsDependencyInjector` (implements `KoinModuleLoader`) with `viewModelOf(::ViewModelName)`.
```kotlin
object PresentationViewModelsDependencyInjector : KoinModuleLoader {
    override val modules: List<Module>
        get() = listOf(
            module {
                viewModelOf(::ExampleViewModel)
            }
        )
}
```

## Error handling & Localization (NO Magic Strings in ViewModels)
- **STRICT RULE**: ViewModels MUST NEVER emit hardcoded/magic strings (`String`) intended for user display (errors, status messages, banners, labels).
- Instead, when a ViewModel needs to communicate a message or error to the UI, it MUST emit a typed state using a `sealed interface` / `sealed class` with `data object` (or `data class` carrying only domain parameters).
- The `:shared:presentation:ui` layer captures that `sealed` object and maps it to localized text via `stringResource(Res.string.<key>)` from `composeResources/values*/strings.xml`.
- Magic values (literal strings, numbers, timeouts, fallback counts) are strictly forbidden; always use named constants or domain/state objects.
- Add retry logic when sensible (e.g. network errors).

## Event handling
One-off events (navigation, taps...) use `SharedFlow`:
```kotlin
private val _events by lazy { MutableSharedFlow<Event>() }
val events: SharedFlow<Event> = _events

suspend fun sendEvent(event: Event) {
    _events.emit(event)
}
```

## Golden rules
1. One purpose: one ViewModel per screen/feature.
2. Named args, one per line.
3. Never expose `MutableStateFlow`; expose immutable `StateFlow`.
4. Complex state → `sealed class`, not many independent `StateFlow`s.
5. Share flows correctly with `shareIn` + `viewModelScope`.
6. Class name ends in `ViewModel`.
7. Avoid initial loads in `init` (can cause memory leaks).
8. Declare private events/state lazily with `by lazy`.
9. Always use trailing commas.

## Testing
- **Unit Tests** live in `src/commonTest/kotlin/` under `features/<feature_name>/<FeatureName>ViewModelTest.kt`.
- **Tools**: `kotlin.test`, `kotlinx.coroutines.test` (`StandardTestDispatcher`, `Dispatchers.setMain` in `@BeforeTest`, `Dispatchers.resetMain` in `@AfterTest`, `runTest`, `advanceTimeBy`, `advanceUntilIdle`).
- **Dependencies**: Instantiate real UseCases backed by an in-memory **Fake** `<FeatureName>Repository` (no Mockito/MockK).
- **Scope**:
  - Verify `StateFlow` transitions (`Loading` → `Success` / `Error` / `Empty`) and typed error/message markers (never raw strings).
  - Verify `SharedFlow` one-off events, search debouncing, out-of-order/stale coroutine cancellation, and retry flows.
- **Execution**:
  - `./gradlew :shared:presentation:viewModels:testAndroidHostTest`
  - `./gradlew :shared:presentation:viewModels:iosSimulatorArm64Test`
