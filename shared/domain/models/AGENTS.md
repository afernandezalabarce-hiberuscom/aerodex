# Module `:shared:domain:models`

## Overview
Pure business models (POJOs / data classes). Heart of the domain. Independent of any data source or UI platform.

## Responsibilities
- Represent business entities and value objects.
- Independent of persistence (Room/DataStore) and network (Ktor/DTOs).
- Pure: no framework logic, no deps beyond Kotlin stdlib.

## Platform constraints
- Pure Kotlin. All code in `commonMain`.
- `expect/actual` for platform logic FORBIDDEN here.
- No Android (`Parcelable`, `Context`) or iOS deps.

## Structure (feature-based)
```text
dev.afalabarce.template.kmp.domain.models.features
└── <feature_name>
    ├── <EntityNameA>.kt
    └── <EntityNameB>.kt
```

## Rules
- Prefer immutable `data class` with `val`.
- Names have NO suffix (`User`, `Product`), not `UserEntity`.
- May hold basic business validation that needs no external service.

```kotlin
package dev.afalabarce.template.kmp.domain.models.features.example

data class Example(
    val id: Long,
    val title: String,
    val description: String
)
```

## Golden rules
1. NEVER add serialization (`@Serializable`) or persistence (`@Entity`) annotations; those belong to `data:models`.
2. Always organize by feature; no loose models in the root package.
3. Keep it simple. UI/DB transformation logic belongs in mappers, not here.
4. Use standard Kotlin types; avoid types needing platform impl.
5. **NO MAGIC VALUES**: Never embed magic strings or hardcoded UI messages in domain models; use typed `enum class` or `sealed interface` / `data object` representations.
