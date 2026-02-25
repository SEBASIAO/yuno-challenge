# Yuno Challenge - Android Project Rules

**MANDATORY**: Before writing ANY code in this project, re-read and apply ALL rules below. No exceptions.

---

## Architecture: Clean Architecture (Strict Layer Separation)

The project follows Clean Architecture with three layers. Dependencies point INWARD only.

```
presentation → domain ← data
```

### Package Structure

```
com.example.sebasiao.yuno.challenge/
├── domain/          # Pure Kotlin. ZERO Android/framework imports.
│   ├── model/       # Entities / domain models
│   ├── repository/  # Repository interfaces (contracts)
│   ├── usecase/     # Use cases (one public function per class)
│   └── exception/   # Domain-specific exceptions
├── data/            # Implements domain contracts. Talks to frameworks.
│   ├── repository/  # Repository implementations
│   ├── remote/      # API services, DTOs, mappers
│   ├── local/       # Room DAOs, entities, mappers
│   └── di/          # Data-layer DI modules
├── presentation/    # UI layer. Android-aware.
│   ├── ui/          # Screens, Composables, Activities
│   ├── viewmodel/   # ViewModels (one per screen/feature)
│   ├── model/       # UI state models, UI events
│   ├── mapper/      # Domain ↔ UI model mappers
│   └── di/          # Presentation-layer DI modules
└── di/              # App-level DI modules
```

---

## CRITICAL RULE: Domain Layer Purity

The `domain/` package must contain **ONLY pure Kotlin code**.

**FORBIDDEN** imports in domain layer:
- `android.*`
- `androidx.*`
- `javax.inject.*`
- `dagger.*`
- `com.google.*`
- `retrofit2.*`
- `okhttp3.*`
- `room.*`
- ANY third-party library

**ALLOWED** imports in domain layer:
- `kotlin.*`
- `kotlinx.coroutines.*`
- `kotlinx.coroutines.flow.*`
- Other `domain/` package classes

If you need an annotation like `@Inject`, do NOT add it in domain. Provide the dependency via constructor and wire it in the `data/` or `di/` layer.

---

## UI Pattern: MVVM (Model-View-ViewModel)

- **View** (Composables/Activities): Observes state, emits events. Zero business logic.
- **ViewModel**: Holds `StateFlow<UiState>`, processes events, calls use cases.
- **Model**: Sealed interfaces for `UiState` and `UiEvent`.

### ViewModel conventions

```kotlin
class FeatureViewModel(
    private val someUseCase: SomeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<FeatureUiState>(FeatureUiState.Loading)
    val uiState: StateFlow<FeatureUiState> = _uiState.asStateFlow()

    fun onEvent(event: FeatureEvent) { /* handle events */ }
}
```

- Expose **only** `StateFlow` (never `MutableStateFlow`) to the View.
- Use `sealed interface` for UI states and events.
- ViewModels must NOT hold references to Context, View, or Activity.

---

## Async: Coroutines & Flows

- Use `suspend fun` for one-shot operations.
- Use `Flow<T>` for reactive streams.
- Use `StateFlow` / `SharedFlow` for state management in ViewModels.
- **NEVER** use `GlobalScope`. Always use structured concurrency (`viewModelScope`, `lifecycleScope`, or custom `CoroutineScope`).
- Repository interfaces in domain return `Flow<T>` or `suspend` results.

### Concurrency & Race Conditions

- Use `Mutex` when protecting shared mutable state in coroutines.
- Use `MutableStateFlow.update { }` (atomic) instead of `_state.value = _state.value.copy(...)`.
- Use `flatMapLatest` for search/debounce patterns to cancel previous work.
- Use `conflate()` or `collectLatest` when only the latest emission matters.
- Use `Channel` (not `SharedFlow(replay=0)`) for one-time events (navigation, snackbars).
- Always specify `Dispatchers` explicitly in data layer: `withContext(Dispatchers.IO)`.
- Never perform I/O on `Dispatchers.Main`.

---

## SOLID Principles

1. **Single Responsibility**: One class = one reason to change. One use case = one business action.
2. **Open/Closed**: Use interfaces and abstractions. Extend behavior without modifying existing code.
3. **Liskov Substitution**: Subtypes must be substitutable for their base types.
4. **Interface Segregation**: Small, focused interfaces. No god-interfaces.
5. **Dependency Inversion**: High-level modules depend on abstractions, not concretions. Domain defines interfaces; data implements them.

---

## Design Patterns to Apply

- **Repository Pattern**: Abstract data sources behind interfaces defined in domain.
- **Use Case / Interactor**: Each business action is a class with `operator fun invoke()` or `suspend operator fun invoke()`.
- **Mapper Pattern**: Separate mapper classes/functions between layers (DTO → Domain, Domain → UI).
- **Factory Pattern**: When object creation is complex.
- **Observer Pattern**: Via `Flow` / `StateFlow`.
- **Strategy Pattern**: When behavior varies by type, use interfaces + implementations.

---

## Code Style & Quality

- **Naming**: Classes `PascalCase`, functions/variables `camelCase`, constants `UPPER_SNAKE_CASE`.
- **Immutability**: Prefer `val` over `var`. Use `data class` with `copy()` for state changes.
- **Nullability**: Avoid `!!`. Use `?.`, `?:`, `let`, `requireNotNull()` with clear messages.
- **Functions**: Keep functions short (<20 lines ideally). One level of abstraction per function.
- **No magic numbers/strings**: Extract constants or enums.
- **Error handling**: Use `Result<T>` or sealed classes for operation outcomes. No bare try/catch in ViewModels.

---

## Testability

- All dependencies via constructor injection.
- Domain use cases receive repository interfaces (easy to mock).
- ViewModels receive use cases (easy to mock).
- Keep Android framework out of testable code.
- Name tests: `fun methodName_condition_expectedResult()`.

---

## UI/UX: Material Design 3 & Minimalist Design

### Design System

- Use **Material Design 3** (Material You) components exclusively via `androidx.compose.material3`.
- Do NOT mix `material` (M2) and `material3` (M3) imports. Use M3 only.
- Define a single `AppTheme` using `MaterialTheme` with custom `ColorScheme`, `Typography`, and `Shapes`.

### Color Palette: Minimalist

- Keep the palette **tight**: one primary color, one secondary, neutral surfaces, and minimal accents.
- Use `MaterialTheme.colorScheme` tokens everywhere. **NEVER** hardcode hex colors in composables.
- Define all colors in the theme. If a new color is needed, add it to the theme, not inline.
- Support dark mode from day one: define both `lightColorScheme` and `darkColorScheme`.
- Prefer high contrast and accessible color combinations (WCAG AA minimum).

```kotlin
// Example palette structure
private val LightColors = lightColorScheme(
    primary = Color(0xFF...),
    onPrimary = Color.White,
    surface = Color(0xFFFAFAFA),
    onSurface = Color(0xFF1C1C1C),
    // Keep it minimal - fewer colors = cleaner UI
)
```

### Usability & Simplicity

- **Less is more**: Every element on screen must earn its place. Remove before adding.
- Generous whitespace and padding. Don't cram elements together.
- Consistent spacing: use a 4dp/8dp/16dp grid system.
- Touch targets minimum **48dp** (Material guideline).
- Clear visual hierarchy: one primary action per screen, secondary actions de-emphasized.
- Loading states, empty states, and error states for every screen. Never leave the user guessing.
- Use `Scaffold`, `TopAppBar`, `FloatingActionButton` from M3 for consistent page structure.

### Compose Component Reuse

- Build a **shared component library** under `presentation/ui/component/`.
- Reusable components examples: buttons, cards, text fields, loading indicators, error views, top bars.
- Components must be **stateless** (hoisted state) and accept parameters via function arguments.
- Use `@Preview` annotations on all reusable components.
- Follow this naming convention: `YunoButton`, `YunoCard`, `YunoTextField`, etc. (prefixed with `Yuno`).

```kotlin
// Correct: stateless, reusable, themed
@Composable
fun YunoCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) { /* content */ }
}
```

### Screen Composable Convention

- Each screen has one root composable: `FeatureScreen(viewModel: FeatureViewModel)`.
- Inside, a **stateless** content composable: `FeatureContent(state: FeatureUiState, onEvent: (FeatureEvent) -> Unit)`.
- This separation allows previewing `FeatureContent` without a ViewModel.

```kotlin
@Composable
fun FeatureScreen(viewModel: FeatureViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    FeatureContent(state = state, onEvent = viewModel::onEvent)
}

@Composable
fun FeatureContent(
    state: FeatureUiState,
    onEvent: (FeatureEvent) -> Unit
) { /* pure UI, no ViewModel dependency */ }
```

### Package Update

```
presentation/
├── ui/
│   ├── component/   # Shared reusable composables (YunoButton, YunoCard, etc.)
│   ├── theme/       # AppTheme, Color, Typography, Shapes
│   └── screen/      # Feature screens organized by feature
```

---

## Feature Implementation: Agent Team Strategy

When implementing a new feature, spawn a **team of parallel agents** (via the Task tool), each responsible for one layer. Every agent writes its own tests. A final Code Review agent validates everything.

### Agent Roles

| Agent | Scope | Responsibilities |
|-------|-------|-----------------|
| **Domain** | `domain/` | Models, repository interfaces, use cases, domain exceptions. Writes unit tests for all use cases. Must produce **pure Kotlin only**. |
| **Data** | `data/` | Repository implementations, DTOs, mappers, API services, local data sources. Writes unit tests for repositories and mappers. |
| **Presentation** | `presentation/` | ViewModel, UiState/UiEvent sealed interfaces, screen composables, reusable components. Writes ViewModel tests. |
| **Code Review** | All layers | Runs AFTER the other agents finish. Validates compliance with ALL rules in this CLAUDE.md. |

### Execution Flow

```
1. Analyze the feature requirements
2. Define the contracts (domain models + repository interfaces) FIRST
3. Launch agents in parallel:
   ┌─────────────────────────────────────────────┐
   │  [Domain Agent]  [Data Agent]  [Presentation Agent]  │
   │   models          DTOs          ViewModel             │
   │   interfaces      impls         UiState/UiEvent       │
   │   use cases       mappers       composables           │
   │   + tests         + tests       + tests               │
   └─────────────────────────────────────────────┘
4. After all agents complete:
   ┌─────────────────────────────────────────────┐
   │              [Code Review Agent]                      │
   │  - Domain purity check (no forbidden imports)         │
   │  - SOLID compliance                                   │
   │  - Concurrency safety                                 │
   │  - M3 / theme compliance                              │
   │  - Test coverage verification                         │
   │  - Stateless composable check                         │
   └─────────────────────────────────────────────┘
5. Fix any issues found by Code Review
```

### Agent Rules

- **Domain agent runs first** (or defines contracts first) since other layers depend on domain interfaces.
- Data and Presentation agents can run **in parallel** once domain contracts exist.
- Each agent **must write tests** for the code it produces. No code without tests.
- Code Review agent uses the Checklist (below) as its validation rubric.
- If Code Review finds violations, fix them before considering the feature complete.

---

## Checklist Before Writing Code

- [ ] Does this code belong in the correct layer?
- [ ] Does the domain layer remain pure Kotlin?
- [ ] Am I depending on abstractions, not concretions?
- [ ] Is shared mutable state protected against race conditions?
- [ ] Am I using structured concurrency?
- [ ] Is the code testable without Android framework?
- [ ] Does the ViewModel only expose immutable StateFlow?
- [ ] Am I using only M3 components (no M2 mixing)?
- [ ] Are colors coming from `MaterialTheme.colorScheme` (no hardcoded hex)?
- [ ] Are composables stateless with hoisted state?
- [ ] Am I reusing shared components from `ui/component/` instead of duplicating?
- [ ] Does every screen handle loading, empty, and error states?

---

## Multi-Module Architecture

Two Gradle modules:

| Module | Type | Package | Description |
|--------|------|---------|-------------|
| `:sdk` | Android Library | `com.yuno.payments.threeds` | 3DS authentication SDK. Never depends on `:app`. |
| `:app` | Application | `com.example.sebasiao.yuno.challenge` | Demo merchant app. Depends on `:sdk`. |

Each module has its own Clean Architecture internally (`domain/`, `data/`, `presentation/`).

Dependencies flow: `:app` → `:sdk`. **NEVER** the reverse.

---

## SDK Public API Conventions

- **Single entry point**: `YunoThreeDSAuthenticator` — the only public-facing class merchants interact with.
- **Everything else is `internal`**: All domain models used only within the SDK are `internal`. Only models needed by the merchant are `public`.
- **Builder pattern** for configuration: `YunoThreeDSConfig.Builder()`.
- **No Activity references stored**: SDK never holds references to Activity. Uses `ActivityResultLauncher` for challenge UI.
- **Plug & play**: SDK works with zero configuration. `YunoThreeDSAuthenticator.initialize(context)` is all that's needed.

---

## Component Naming

| Module | Prefix | Examples |
|--------|--------|---------|
| `:sdk` | `ThreeDS` | `ThreeDSOtpField`, `ThreeDSChallengeScreen`, `ThreeDSTheme` |
| `:app` | `Yuno` | `YunoCard`, `YunoButton`, `YunoTextField` |

---

## DI Approach

**Manual constructor injection only.** No Hilt, Dagger, or Koin.

| Container | Scope | Lifecycle |
|-----------|-------|-----------|
| `SdkContainer` | `object` (Kotlin singleton) | Process-scoped. Initialized in `YunoThreeDSAuthenticator.initialize()`. Survives rotation, config changes. |
| `AppContainer` | Property of `Application` class | Process-scoped. Created in `MerchantApp.onCreate()`. Accessed via `(application as MerchantApp).container`. |

- ViewModels created via `ViewModelProvider.Factory` pulling deps from containers.
- Containers survive configuration changes because they're tied to `Application` lifecycle.
- `SavedStateHandle` in ViewModels for surviving process death.

---

## Concurrency Rules (Addendum)

In addition to the base concurrency rules above:

- **Singleton initialization**: Use `@Volatile` flag + `@Synchronized` method to prevent double init.
- **Mutable-state repositories**: `Mutex` is **mandatory** (not optional) for any repository with in-memory mutable state.
- **SharedPreferences access**: Always behind `Mutex` + `withContext(Dispatchers.IO)`.
- **Process death**: Use `SavedStateHandle` in ViewModels with important transient state (OTP entry, timestamps).
- **Rapid taps**: ViewModels must include `isProcessing` flag in state to disable UI during async operations.

---

## Development Methodology: Test Driven Development (TDD)

**ALL code** is developed with strict TDD: Red → Green → Refactor.

### Cycle

1. **Red**: Write the test FIRST. It must fail (compilation error or assertion failure).
2. **Green**: Write the MINIMUM code to make the test pass. No over-engineering.
3. **Refactor**: Clean up without changing behavior. Tests must still pass.

### Rules

- No production file is created without its corresponding test file already existing.
- Tests define the contract: the test is the specification, the code is the implementation.
- Order per feature: test file first → implementation file second.
- Mock dependencies with **MockK**.
- Coroutine tests with `runTest` + `TestDispatcher`.
- ViewModel tests with **Turbine** for StateFlow emission validation.
- Concurrency tests: multiple coroutines writing simultaneously → no crashes, correct counts.

---

## Git Flow

- **One atomic commit per phase/feature.** Commit includes both tests and implementation.
- Commit only AFTER all tests for the phase pass.
- No partial commits within a phase. Fix issues before committing.
- **Format**: Conventional Commits — `type(scope): description`.
- **Push** to remote after each commit.

---

## Updated Package Structure

```
:sdk (com.yuno.payments.threeds)
├── domain/
│   ├── model/          # Transaction, RiskPolicy, RiskAssessment, etc.
│   ├── repository/     # RiskRepository, DeviceFingerprintRepository, etc.
│   └── usecase/        # EvaluateTransactionRiskUseCase, etc.
├── data/
│   ├── repository/     # DefaultRiskRepository, InMemoryVelocityRepo, etc.
│   └── risk/           # RiskFactor interface, AmountRiskFactor, RiskScoreEngine
├── presentation/
│   ├── theme/          # ThreeDSTheme, ThreeDSColors
│   ├── component/      # ThreeDSOtpField, ThreeDSTransactionSummary
│   └── challenge/      # ChallengeViewModel, ChallengeScreen, ChallengeActivity
├── api/                # YunoThreeDSAuthenticator, YunoThreeDSConfig
└── di/                 # SdkContainer

:app (com.example.sebasiao.yuno.challenge)
├── domain/
│   ├── model/          # SampleTransaction, TransactionScenario
│   ├── repository/     # SampleTransactionRepository
│   └── usecase/        # GetSampleTransactionsUseCase, ProcessTransactionUseCase
├── data/
│   └── repository/     # HardcodedSampleTransactionRepository
├── presentation/
│   ├── theme/          # AppTheme, AppColors
│   ├── ui/
│   │   ├── component/  # YunoButton, YunoCard, YunoTextField, etc.
│   │   └── screen/     # TransactionListScreen, TransactionFormScreen, ResultScreen
│   ├── viewmodel/      # TransactionListViewModel, TransactionFormViewModel, ResultViewModel
│   ├── model/          # UiState and UiEvent sealed interfaces
│   └── navigation/     # NavHost, routes
├── di/                 # AppContainer
└── MerchantApp.kt      # Application class
```
