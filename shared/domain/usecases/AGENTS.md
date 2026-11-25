# Module `:shared:domain:usecases`

## Overview
Atomic business logic. Each use case = one business action, orchestrating repositories.

## Responsibilities
- Implement app-specific business rules.
- Coordinate one or more repositories.
- Atomic: one task per class.

## Platform constraints
- Pure Kotlin in `commonMain`.
- No persistence/network/UI deps.

## Structure (feature-based)
```text
dev.afalabarce.template.kmp.domain.usecases.features
└── <feature_name>
    └── <ActionName>UseCase.kt
```

## Design + errors
- Call interface: always `operator fun invoke` (call like `useCase()`).
- Async: `suspend` for one-shot, `Flow` for streams.
- Wrap results in `Result<T>`.
- Return types:
    - `suspend operator fun invoke(...): Result<T>`
    - `operator fun invoke(...): Flow<Result<T>>`

```kotlin
package dev.afalabarce.template.kmp.domain.usecases.features.example

import dev.afalabarce.template.kmp.domain.models.features.example.ExampleEntity
import dev.afalabarce.template.kmp.domain.repository.features.example.ExampleRepository
import kotlinx.coroutines.flow.Flow

class GetExampleItemsUseCase(
    private val repository: ExampleRepository
) {
    /** Get the example items flow wrapped in Result. */
    operator fun invoke(): Flow<Result<List<ExampleEntity>>> {
        return repository.getEntities()
    }
}
```

## DI (Koin)
Register each use case in `DomainUseCasesDependencyInjector` (implements `KoinModuleLoader`) with `factoryOf(::UseCaseName)`.

```kotlin
object DomainUseCasesDependencyInjector : KoinModuleLoader {
    override val modules: List<Module>
        get() = listOf(
            module {
                factoryOf(::GetExampleItemsUseCase)
            }
        )
}
```

## Golden rules
1. One purpose. If a use case does too much, split it.
2. Named args, one per line.
3. Never drop the `Result<T>` wrapper; presentation depends on it for error/loading states.
4. Class name ends in `UseCase`.
5. **NO MAGIC VALUES**: Never use magic strings or numbers in use cases; rely on named constants or typed domain models.

## Testing
- **Unit Tests** live in `src/commonTest/kotlin/` under `features/<feature_name>/<FeatureName>UseCasesTest.kt`.
- **Tools**: `kotlin.test` (`@Test`, `assertEquals`, `assertFailsWith`, etc.) and `kotlinx.coroutines.test.runTest`.
- **Fakes over Mocks**: Implement lightweight in-memory **Fake** classes for `<FeatureName>Repository` in the test file (never use reflection-based mocking libraries).
- **Scope**: Test input validation/normalization (e.g., trimming, case normalization, rejecting blank/invalid params), business rules, multi-repository orchestration, and `Result` error propagation.
- **Execution**:
  - `./gradlew :shared:domain:usecases:testAndroidHostTest`
  - `./gradlew :shared:domain:usecases:iosSimulatorArm64Test`
