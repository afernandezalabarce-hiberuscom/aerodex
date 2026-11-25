# Module `:shared:domain:repository`

## Overview
Defines repository contracts (interfaces) only. Abstracts data origin from use cases.

## Responsibilities
- Define methods to access/manipulate business data.
- Hide data-layer details.
- Type- and error-safe responses.

## Platform constraints
- Pure Kotlin, no `androidMain`/`iosMain`.
- Interfaces only; impl lives in `:shared:data:repository`.
- No persistence/network deps.

## Structure (feature-based)
```text
dev.afalabarce.template.kmp.domain.repository.features
└── commonMain
    └── <feature_name>
        └── <FeatureName>Repository.kt
```

## Functions + errors
- Async: `suspend` for one-shot, `Flow` for streams.
- Wrap results: every function returns `Result<T>` (`T` = domain model).
- Return types:
    - `suspend fun doSomething(...): Result<T>`
    - `fun observeSomething(...): Flow<Result<T>>`

```kotlin
package dev.afalabarce.template.kmp.domain.repository.features.example

import dev.afalabarce.template.kmp.domain.models.features.example.ExampleEntity
import kotlinx.coroutines.flow.Flow

interface ExampleRepository {
    /** Observe entities from the data source. */
    fun getEntities(): Flow<Result<List<ExampleEntity>>>

    /** Sync with the remote source. */
    suspend fun syncEntities(): Result<Unit>
}
```

## Golden rules
1. Create interfaces inside their feature package.
2. Never return raw types (`List<T>`); always `Result<T>` to force error handling upstream.
3. File + interface name end in `Repository`.
4. Briefly document each method, especially side effects (e.g. sync).
5. **NO MAGIC VALUES**: Do not hardcode magic strings or numeric literals; use named constants or typed domain exceptions/states.
