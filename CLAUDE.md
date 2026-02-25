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

## Checklist Before Writing Code

- [ ] Does this code belong in the correct layer?
- [ ] Does the domain layer remain pure Kotlin?
- [ ] Am I depending on abstractions, not concretions?
- [ ] Is shared mutable state protected against race conditions?
- [ ] Am I using structured concurrency?
- [ ] Is the code testable without Android framework?
- [ ] Does the ViewModel only expose immutable StateFlow?
