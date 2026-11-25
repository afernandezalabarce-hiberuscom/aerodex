# Module `:shared:core:common`

## Role
You are an expert KMP dev building reusable utility APIs/libraries consumed by humans or AI.

## Scope
- Host project-wide cross-cutting code only: functions, classes, extensions on base/primitive types.
- NEVER add tech that breaks Clean Architecture or SOLID.
- Add a function here only if it is truly cross-cutting AND needs no third-party dependency that pulls in incompatible modules.

## Structure
```text
core:common/
├── src/
│   ├── commonMain/
│   │   └── kotlin/dev.afalabarce.template.kmp.core.common/
│   │       ├── di/                 # DI; usually nothing to add
│   │       │   └── KoinModuleLoader.kt
│   │       ├── extensions/         # Kotlin extensions
│   │       └── utils/              # common utils
│   ├── androidMain/
│   │   └── kotlin/dev.afalabarce.template.kmp.core.common/
│   │       ├── extensions/
│   │       │   ├── Constants.kt    # general constants (mappers, extension fns)
│   │       │   ├── Encryption.kt   # encrypt/decrypt (expect/actual)
│   │       │   └── Extensions.kt   # general extensions on base types
│   │       ├── serializers/
│   │       └── utilities/
│   ├── iosMain/
│   │   └── kotlin/dev.afalabarce.template.kmp.core.common/
│   │       ├── extensions/
│   │       ├── serializers/
│   │       └── utils/
│   ├── commonTest/
│   ├── androidTest/
│   └── iosTest/
├── build.gradle.kts
└── AGENTS.md
```

## Rules
- English for names + comments.
- **NO MAGIC VALUES**: Never introduce hardcoded magic strings, numbers, or dimensions; always define named constants or typed tokens.
- Keep code pure; no business logic, no UI, no persistence.
- Prefer small extensions over big util classes.

## Testing
- **Unit tests** live in `src/commonTest/kotlin/` (and `androidHostTest` / `iosTest` only for `expect/actual` platform-specific utilities).
- **Tools**: `kotlin.test` (`@Test`, `assertEquals`, `assertFailsWith`, etc.) and `kotlinx.coroutines.test` (`runTest`) for suspending helpers.
- **Scope**: Test pure extensions, formatters, serializers, and edge cases (nulls, empty inputs, boundaries) deterministically without external dependencies.
- **Execution**: Ensure `withHostTest {}` is enabled in `build.gradle.kts` under `kotlin.android` before running JVM host tests:
  - `./gradlew :shared:core:common:testAndroidHostTest`
  - `./gradlew :shared:core:common:iosSimulatorArm64Test`
