# Module `:shared:presentation:ui`

Build UI with Clean Architecture, MVVM, Jetpack Compose Multiplatform. Focus: accessibility, performance, state hoisting. For AI agents and humans.

## Role
Contains only UI: screens, visual navigation, composed components, feature content.

### Responsibilities
- Render state exposed by ViewModels.
- Encapsulate UI navigation.
- Compose screens + reusable subcomponents per feature.
- Keep state, events, rendering separate.
- Ensure EAA + WCAG 2.2 accessibility.

### Forbidden
- Access data sources, repositories, or use cases.
- Implement business logic.
- Duplicate state logic owned by `presentation:viewModels`.
- Couple screens to platform/native APIs unless strictly UI.
- Use raw standard composables when a `:shared:core:ui` wrapper exists.
- **Use magic values**: hardcoded strings, literal dimensions (`16.dp`), literal colors, or unlabelled numeric constants are strictly forbidden. All UI text MUST come from `composeResources/values*/strings.xml` via `stringResource(Res.string.<key>)`, and all ViewModel text states MUST arrive as `sealed` objects that the UI maps to `stringResource(...)`.

## Principles

### Clean Architecture
- UI depends on `presentation:viewModels`, `core:ui`, common utils.
- UI knows nothing about persistence/network/infrastructure.
- Data for painting arrives already resolved from the ViewModel.
- Every standard platform composable is consumed via a `:shared:core:ui` wrapper, unless none exists.

### MVVM
- Each feature has its own ViewModel in `presentation:viewModels`.
- Each Screen observes one source of truth.
- Screen only: collects state, delegates events, maps state to UI.

### State hoisting
- State lives at the highest reasonable level.
- Reusable composables are preferably stateless.
- Children keep only ephemeral UI-local state.
- Events always go up via callbacks.

### Accessibility
- Functional requirement, not an extra.
- Explicitly meet EAA + WCAG 2.2.
- Never rely on color/size/position alone.

### Performance
- Optimize recompositions; avoid needless allocations.
- Keep params stable.
- Prefer small, pure, predictable composables.

## Feature structure
Everything by feature. Each feature has a main Screen + needed pieces in own packages.
```text
dev.afalabarce.template.kmp.presentation.ui
└── features
    └── <feature_name>
        ├── navigation
        │   └── <Feature>Navigation.kt
        ├── screen
        │   └── <Feature>Screen.kt
        ├── components
        │   └── ...
        ├── sections
        │   └── ...
        └── previews
            ├── previewdata
            │   └── ...
            └── ...
```

### Organization rules
- Each feature has an entry `Screen`.
- Screen delegates to subcomponents/sections/specific pieces.
- Big Screen → split by responsibility into more packages.
- File name describes its content precisely.
- Tree must make Screen, subcomponents, previews, preview data easy to find.

### Minimal packages per feature
- `navigation/`: routes, destinations, visual args.
- `screen/`: main Screen wiring state + events.
- `components/`: reusable composables for the feature.
- `sections/`: larger visual blocks within a Screen.
- `previews/`: screen + component previews.
- `previewdata/`: simple reusable preview data when needed.

## Screen
### Must
- Receive paint-ready state.
- Receive event callbacks.
- Collect ViewModel state with `collectAsStateWithLifecycle()`.
- Delegate rendering to small composables.
- Keep minimal local state.

### Must not
- Call repositories or use cases.
- Hold business logic.
- Repeat domain→UI conversions.
- Mix navigation, state, composition without separation.

## Composition levels
### Screen
Feature's main container.
- Wires ViewModel + UI. Handles UI side effects if needed. Exposes callbacks. Small + readable.

### Sections
Group large visual blocks.
- For complex screens. Stateless when possible. Do not know the ViewModel.

### Components
Reusable pieces.
- As generic as possible. Take state via params. Emit events via callbacks.

### Preview data
Previews need NO dedicated UI models.
- Prefer inline data in the preview. Put repeated/complex data in `previewdata/`. Keep it simple, stable, maintainable.

## State hoisting rules (mandatory)
- State rises to the first level that truly needs it.
- A reusable composable creates/manages no business state.
- A child does not decide the main data to show if it depends on screen state.
- User events emit via clearly-named callbacks.
- Internal temporary state must be: local, small, derived only from visual interaction, easy to extract if it grows.

### Correct
```kotlin
@Composable
fun UserSearchScreen(
    uiState: UserSearchUiState,
    onQueryChange: (String) -> Unit,
    onRetry: () -> Unit,
) {
    UserSearchContent(
        query = uiState.query,
        results = uiState.results,
        isLoading = uiState.isLoading,
        onQueryChange = onQueryChange,
        onRetry = onRetry,
    )
}
```

### Wrong
```kotlin
@Composable
fun UserSearchContent() {
    var query by remember { mutableStateOf("") }
}
```

## Accessibility: EAA + WCAG 2.2
Meet perceivable, operable, understandable, robust explicitly.

### Requirements
- Every interactive control reachable by keyboard, TalkBack, VoiceOver.
- Interactive elements have sufficient touch target.
- Text keeps adequate contrast.
- Color is never the only state signal.
- Clear semantic hierarchy.
- Error messages understandable + tied to the affected element.
- Content survives font-size increase.
- Loading/empty/error/success states announceable semantically when relevant.

### Semantics
Prefer `semantics` first. Apply as needed: `contentDescription`, `stateDescription`, `role`, `heading`, `onClickLabel`, `paneTitle`, `liveRegion`, `focusOrder`, `testTag` (only for tests).

### Forms + validation
- Each field has a visible label or accessible equivalent.
- Errors tied to the field. `required` perceivable. Placeholders are not labels.

### Contrast + legibility
- Min contrast for normal text: 4.5:1. Enough contrast for icons, borders, interactive states.
- Avoid text baked into images. Avoid fixed sizes blocking reflow/scaling.

### Navigation + focus
- Logical focus order. Critical elements focusable. Important actions not gated behind touch-only gestures.

## Compose optimization
### General
- Small, pure composables. No costly work in the body.
- No needless lists/maps/temp objects per recomposition.
- Prefer stable, immutable params.
- `remember` only for state/calc that must persist.
- `derivedStateOf` when a value derives from observable state and is costly to recompute.
- `rememberUpdatedState` when an effect needs the latest lambda/value without restarting.

### Lists + grids
- Use `LazyColumn`/`LazyRow` for large collections. Provide stable `key`. Avoid recomposing needless items.

### State + recomposition
- Expose only minimal paint state. Prefer `StateFlow` + `collectAsStateWithLifecycle()` in Screen.
- Avoid redundant observations. Derive at the UI edge, not in each child.

### Stability
- Keep lambdas/models stable. For heavily-used UI models, consider `@Immutable`/`@Stable`.

### Side effects
- `LaunchedEffect` only for real composition effects.
- No navigation/toasts/imperative actions from purely visual functions without explicit control.

## Composition conventions
- Named args, one per line. `modifier` is the first optional param.
- Public composables have clear, intent-oriented names. Avoid vague/over-generic APIs.
- Prefer stateless functions for reusable components.
- Separate data input, events, rendering.

## Previews
Every Screen + relevant component needs previews.
- Live in `previews/`. Wrap in `AppMaterialTheme`.
- Show normal, empty, loading, error, and accessibility-relevant variants.
- Easy to run + understand. Do not force dedicated UI models just to preview.
- Recommended: simulate text sizes, long content, visible error states; help spot contrast/layout issues.

## Navigation
- Visual navigation lives in UI.
- Routes explicit + centralized per feature/flow.
- Screen ignores needless global navigation details.
- Navigation events propagate via callbacks.

## Dependencies
Allowed: `:shared:core:ui`, `:shared:core:common`, `:shared:presentation:viewModels`, `androidx.compose.*`, `androidx.lifecycle.compose.*`, `androidx.navigation.*`, generated Compose resources.

Not allowed: `data:*`, `domain:*`, infrastructure impls, raw standard composables when a `:shared:core:ui` wrapper exists.

## Screen checklist
- [ ] One main Screen per feature.
- [ ] State received via params or clearly from the ViewModel.
- [ ] `collectAsStateWithLifecycle()` used when needed.
- [ ] State hoisted; children mostly stateless.
- [ ] No data source / repository / use case access.
- [ ] No raw standard composables where a `:shared:core:ui` wrapper exists.
- [ ] Previews buildable without dedicated UI models.
- [ ] Events go up via callbacks.
- [ ] Useful, representative previews exist.
- [ ] UI accessible with clear semantics.
- [ ] Correct contrast + interactive target sizes.
- [ ] No avoidable recompositions/allocations.
- [ ] Layout survives large text + long content.

## Accessibility checklist (EAA/WCAG 2.2)
- [ ] Each control has an accessible name.
- [ ] Each control has a proper role/semantics.
- [ ] Each important state programmatically detectable.
- [ ] Color is not the only signal.
- [ ] Logical focus order.
- [ ] Legible with font scaling.
- [ ] Errors announced + tied to field.
- [ ] Interactive elements have sufficient touch size.
- [ ] Info not dependent on animation/shape/position alone.

## Performance checklist
- [ ] Composables small + focused.
- [ ] No needless objects in frequent recompositions.
- [ ] Lists use stable keys.
- [ ] `remember`/`derivedStateOf` only when valuable.
- [ ] Collected state minimal + concrete.
- [ ] No hidden side effects in pure composables.

## Final rule
If a UI choice improves looks but worsens accessibility, maintainability, or performance — do not implement it.

## Testing
All UI tests live in `src/commonTest/kotlin/` and use `compose.uiTest` (`androidx.compose.ui.test.v2.runComposeUiTest`), `kotlin.test`, and `kotlinx.coroutines.test`.

### Multiplatform Runner Setup
- Annotate Compose UI test classes with `@CommonRunWith(PlatformTestRunner::class)` (`expect/actual` in `common/PlatformTestRunner.kt`).
- On Android (`androidHostTest/`), `PlatformTestRunner` maps to `RobolectricTestRunner` with `robolectric.properties` (`sdk=35`) and `withHostTest { isIncludeAndroidResources = true }` in `build.gradle.kts`.
- On iOS (`iosTest/`), `PlatformTestRunner` runs natively on the iOS simulator without Robolectric.

### 1. UI Component Tests (Unit / Isolated)
- File: `features/<feature_name>/<FeatureName>UiComponentsTest.kt`.
- Wrap each composable in `AppMaterialTheme {}` and pass explicit state + callback lambdas.
- **Verify**: rendering (`assertIsDisplayed`), interactions (`performClick`, `performTextInput`, `performScrollTo`), callback invocations, and WCAG 2.2 accessibility semantics (`SemanticsProperties.Role`, `contentDescription`, `stateDescription`, `assertIsSelected`).

### 2. UI End-to-End (E2E) Tests
- File: `features/<feature_name>/<FeatureName>EndToEndTest.kt`.
- Setup `StandardTestDispatcher` via `Dispatchers.setMain` (`@BeforeTest`) and `Dispatchers.resetMain` (`@AfterTest`).
- Wire the real `Screen` / navigation flow with a real `ViewModel`, real `UseCases`, real `RepositoryImpl`, real `RemoteDataSourceImpl` (backed by Ktor `MockEngine` + `Ktorfit` + `Json`), and an in-memory **Fake** `LocalDataSource`.
- **Verify**: complete user journeys (initial load, search/filter, navigation between screens, favorite/mutation persistence across tabs, and error/retry recovery).

### Execution
- `./gradlew :shared:presentation:ui:testAndroidHostTest`
- `./gradlew :shared:presentation:ui:iosSimulatorArm64Test`
