# Module `:shared:core:ui`

## Role
Expert KMP UI dev. Focus: reusable composables, Android/iOS consistency, [WCAG 2.2](https://www.w3.org/TR/WCAG22/) accessibility, recomposition performance. Consumed by humans or AI.

## Purpose
- Global theme: `AppMaterialTheme`.
- Central dimensions/spacing: `MobileDimens`.
- Wrapper components so presentation never uses raw Compose components directly.
- Preview pattern for every component in a dedicated package.

## Structure
```text
shared/core/ui/
├── build.gradle.kts
├── AGENTS.md
└── src/
    ├── commonMain/
    │   ├── kotlin/io/afalabarce/template/kmm/core/ui/
    │   │   ├── composables/        # multiplatform wrappers/components
    │   │   │   ├── RadioGroup.kt   # generic RadioButtonGroup<T>
    │   │   │   └── [more...]
    │   │   ├── di/
    │   │   │   ├── CoreUiDependencyInjector.kt       # expect/actual interface
    │   │   │   ├── CoreUiDependencyInjector.android.kt
    │   │   │   └── CoreUiDependencyInjector.ios.kt
    │   │   ├── previews/           # composable previews
    │   │   │   ├── RadioGroupPreviews.kt
    │   │   │   └── [more...]
    │   │   └── theme/
    │   │       ├── AppMaterialTheme.kt   # main theme (expect/actual)
    │   │       ├── Color.kt
    │   │       ├── Dimens.kt             # dimensions (expect/actual)
    │   │       ├── Dimens.android.kt
    │   │       ├── Dimens.ios.kt
    │   │       ├── Shapes.kt
    │   │       ├── Theme.android.kt
    │   │       ├── Theme.ios.kt
    │   │       └── Type.kt
    │   └── composeResources/       # [auto-generated] Compose resources
    ├── androidMain/
    │   ├── kotlin/io/afalabarce/template/kmm/core/ui/
    │   │   ├── di/CoreUiDependencyInjector.android.kt
    │   │   └── theme/{Dimens.android.kt, Theme.android.kt}
    │   └── res/                    # Android resources (strings, colors, dimens...)
    ├── iosMain/
    │   └── kotlin/io/afalabarce/template/kmm/core/ui/
    │       ├── di/CoreUiDependencyInjector.ios.kt
    │       └── theme/{Dimens.ios.kt, Theme.ios.kt}
    ├── commonTest/
    ├── androidUnitTest/
    └── iosTest/
```

## Package `composables`
- One component per file. File name = component (`RadioGroup.kt`, `TextField.kt`).
- Make generic when possible.
- CRITICAL: every composable here MUST have a preview file in `previews/`.

```kotlin
// composables/RadioGroup.kt
@Composable
inline fun <reified T> RadioButtonGroup(
    selectedValue: T?,
    radioButtonValues: Array<T>,
    modifier: Modifier = Modifier,
    crossinline radioButtonLabel: @Composable (T) -> Unit = { },
    crossinline radioButtonBody: @Composable (T) -> Unit = { },
    borderStroke: BorderStroke? = null,
    dividerHeight: Dp = 4.dp,
    excludedValues: Array<T> = emptyArray(),
    radioButtonItemShape: Shape = MaterialTheme.shapes.medium,
    crossinline onCheckedChanged: (T) -> Unit
)
```

## Package `previews`
- One preview file per component. Name: `{Component}Previews.kt`.
- Wrap in `AppMaterialTheme {}`. Annotate with `@Preview`.
- Multiple variants. Use `remember/mutableStateOf` for interactivity.

```kotlin
// previews/RadioGroupPreviews.kt
enum class PreviewOption { OPTION_ONE, OPTION_TWO, OPTION_THREE }

@Preview
@Composable
fun RadioButtonGroupBasicPreview() {
    AppMaterialTheme {
        val (selectedOption, setSelectedOption) = remember { mutableStateOf(PreviewOption.OPTION_ONE) }
        RadioButtonGroup(
            selectedValue = selectedOption,
            radioButtonValues = arrayOf(
                PreviewOption.OPTION_ONE,
                PreviewOption.OPTION_TWO,
                PreviewOption.OPTION_THREE,
            ),
            radioButtonLabel = { option -> Text(text = "Option: ${option.name}") },
            onCheckedChanged = { option -> setSelectedOption(option) },
        )
    }
}

@Preview @Composable fun RadioButtonGroupWithBorderPreview() { /* border variant */ }
@Preview @Composable fun RadioButtonGroupWithBodyPreview() { /* extra-description variant */ }
```

## Package `theme`
- `AppMaterialTheme.kt`: applies customized Material 3 theme (expect/actual).
- `Color.kt`: light/dark color schemes.
- `Type.kt`: Material 3 typography.
- `Shapes.kt`: shapes/border radius.
- `Dimens.kt`: `MobileDimens` (expect/actual per platform).

Rules:
- Material 3 only. Material 2 components FORBIDDEN.
- Dimensions are platform-specific via `expect/actual`.
- Use `Dimens` values, never hardcoded spacing.

## Package `di`
- Implement `KoinModuleLoader`.
- Platform deps: `expect fun getPlatformInjects()`.

## Add a composable
1. Create `composables/MyNewComponent.kt`:
```kotlin
@Composable
fun MyNewComponent(
    title: String,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    onAction: () -> Unit = {},
) {
    // follow project conventions
}
```
2. Create `previews/MyNewComponentPreviews.kt` (2-3 variants, wrapped in `AppMaterialTheme {}`, named args per line):
```kotlin
@Preview
@Composable
fun MyNewComponentBasicPreview() {
    AppMaterialTheme {
        MyNewComponent(title = "Basic Title", onAction = { })
    }
}

@Preview
@Composable
fun MyNewComponentCustomStatePreview() {
    AppMaterialTheme {
        MyNewComponent(title = "Custom State Title", onAction = { })
    }
}
```
3. Update `di/CoreUiDependencyInjector.kt` if needed (context, resources...).
4. Document here if significant.

## Modify dimensions
Edit `Dimens.{android,ios}.kt`:
```kotlin
// commonMain/theme/Dimens.kt (expect)
expect object MobileDimens {
    val paddingSmall: Dp
    val paddingMedium: Dp
    val paddingLarge: Dp
}

// androidMain/theme/Dimens.android.kt (actual)
actual object MobileDimens {
    actual val paddingSmall: Dp = 8.dp
    actual val paddingMedium: Dp = 16.dp
    actual val paddingLarge: Dp = 24.dp
}

// iosMain/theme/Dimens.ios.kt (actual)
actual object MobileDimens {
    actual val paddingSmall: Dp = 10.dp
    actual val paddingMedium: Dp = 18.dp
    actual val paddingLarge: Dp = 26.dp
}
```

## Update global theme
```kotlin
@Composable
fun AppMaterialTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
```

## Conventions
- **NO MAGIC VALUES (MANDATORY)**: Magic values are strictly forbidden across all files. Never hardcode strings, dimensions (`Dp`/`Sp`), colors, or numeric constants inline. Use `MobileDimens` / `AppMaterialTheme.dimens` for dimensions, `AppMaterialTheme.colorScheme` / `MaterialTheme.colorScheme` / theme tokens in `Color.kt` for colors, and `composeResources` (`stringResource`) or caller-provided localized strings for text.
- Naming: composables PascalCase (`RadioButtonGroup`); file = main composable; previews `{Component}Previews.kt`; private fns start with `_`.
- Spacing: ALWAYS `MobileDimens`, never hardcoded Dp.
    - Good: `Spacer(modifier = Modifier.size(MobileDimens.paddingMedium))`
    - Bad: `Spacer(modifier = Modifier.size(16.dp))`
- Required params first; `modifier` is the first optional param; content/main-event param is last.
- Calls use named args, one per line.
- Preview decorators: `@Preview` + `@Composable`, body wrapped in `AppMaterialTheme {}`.
- Performance: `remember` for persisted values, stable lambdas, no heavy work in body (`remember`/`derivedStateOf`).
- Accessibility (WCAG 2.2): prefer `semantics` over `contentDescription`; body ≥12sp, interactive ≥16sp; contrast ≥4.5:1; keyboard + TalkBack (Android) / VoiceOver (iOS) navigable.

## New component checklist
- [ ] Composable in `composables/{Component}.kt`
- [ ] Preview file in `previews/{Component}Previews.kt`
- [ ] ≥2 preview variants
- [ ] Previews wrapped in `AppMaterialTheme {}`
- [ ] Uses `MobileDimens`
- [ ] Named args per line
- [ ] No raw Compose components (except Material 3 base)
- [ ] `semantics` preferred over `contentDescription`
- [ ] Compiles
- [ ] Previews fine at different screen sizes
- [ ] Documented here if significant

## Existing components
### RadioButtonGroup (`composables/RadioGroup.kt`)
- Previews: `previews/RadioGroupPreviews.kt`
- Generic `<T>` radio button group.
- Variants: Basic, WithBorder, WithBody, WithExcludedValues.

## Gradle tasks
```bash
./gradlew :shared:core:ui:compileAndroidMain
./gradlew :shared:core:ui:compileIosMain
./gradlew :shared:core:ui:testAndroidHostTest
./gradlew :shared:core:ui:iosSimulatorArm64Test
./gradlew :shared:core:ui:build
```

## Testing
- **UI Component (Unit) Tests** live in `src/commonTest/kotlin/` using `compose.uiTest` (`androidx.compose.ui.test.v2.runComposeUiTest`) and `kotlin.test`.
- Wrap every tested composable in `AppMaterialTheme {}`.
- **Scope**: Verify visual rendering, state/variant switching, callback invocation (`performClick`, `performTextInput`), and WCAG 2.2 accessibility semantics (`SemanticsProperties`, `contentDescription`, `stateDescription`, `Role`).
- **Android Host Runner**: To run Compose UI tests on JVM via Robolectric, enable `withHostTest { isIncludeAndroidResources = true }` in `build.gradle.kts`, add `robolectric` + `compose.ui.test.manifest` to `androidHostTest`, and annotate test classes with `@CommonRunWith(PlatformTestRunner::class)`.
- **Previews**: Every new component must also compile its `@Preview` variants (`compileAndroidMain`, `compileIosMain`).

## Links
- Material 3: https://m3.material.io/
- Compose: https://developer.android.com/jetpack/compose/documentation
- WCAG 2.2: https://www.w3.org/TR/WCAG22/
- KMP: https://kotlinlang.org/docs/multiplatform.html
