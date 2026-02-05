# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build to verify compilation and checks
./gradlew build

# Run all checks (lint, detekt, ktlint, tests)
./gradlew check --no-daemon

# Run a single test class
./gradlew :feat:projects:fe:driving:impl:jvmTest --tests "*ProjectDetailsEditViewModelTest"

# Platform-specific builds
./gradlew :androidApp:assembleDebug          # Android
./gradlew :composeApp:run                     # Desktop (JVM)
./gradlew :server:impl:run --no-daemon        # Server (Ktor)
./gradlew :composeApp:wasmJsBrowserDevelopmentRun  # Web (Wasm)
./gradlew :composeApp:jsBrowserDevelopmentRun      # Web (JS)
# iOS: Open iosApp/ in Xcode
```

## Architecture

This is a **Kotlin Multiplatform** project using **Clean Architecture + Hexagonal (Ports & Adapters)** pattern.

### Module Structure

```
feat/<feature>/
├── business/     # Platform-agnostic domain models (no dependencies except :lib:core)
├── be/           # Backend
│   ├── app/      # Use cases
        ├── api/  # Public use case interfaces
        └── impl/ # Use case implementations
│   ├── ports/    # Data source interfaces
│   ├── driven/   # Port implementations (database)
│   └── driving/  # HTTP routes (Ktor)
│       ├── contract/  # API contracts
│       └── impl/      # Route implementations
└── fe/           # Frontend
    ├── app/      # Use cases
        ├── api/  # Public use case interfaces
        └── impl/ # Use case implementations
    ├── ports/    # API and DB interfaces
    ├── driven/   # Implementations
    │   ├── impl/ # Production (HTTP, SQLite)
    │   └── test/ # Fake implementations for testing
    └── driving/  # Compose screens
        ├── api/  # Public screen contracts
        └── impl/ # Screen implementations + ViewModels

lib/              # Domain-agnostic reusable libraries
├── core/         # Core utilities (used anywhere)
├── sync/         # Sync logic
├── design/       # Design system components
├── network/      # HTTP client setup
└── routing/      # Routing utilities
```

Detailed description is available in [Project Structure](docs/project_structure.md)`.

### Key Patterns

**Dependency Injection:** Koin. Modules composed in `composeApp/.../di/AppModule.kt`.

**State Management:** ViewModel + `StateFlow<UiState>` with separate `errorsFlow` for error events. Use `collectAsStateWithLifecycle()` in Composables.

**Data Results:**
- `OfflineFirstDataResult<T>` (Success | ProgrammerError) - offline-first frontend operations
- `OnlineDataResult<T>` (Success | Failure) - online-only operations

**Testing:** Fake implementations in `driven/test/` modules. Tests extend `FrontendKoinInitializedTest`
or `BackendKoinInitializedTest` which handle all DI setup including dispatcher setup. Override
`additionalKoinModules()` to bind fakes: `singleOf(::FakeXxxDb) bind XxxDb::class`. Use Turbine for
StateFlow testing.

## Tech Stack

- Kotlin 2.3.0, JDK 21
- Compose Multiplatform 1.10.0 + Material3 Expressive
- Ktor (server + client)
- SQLDelight on frontend (multiplatform database)
- Koin 4.2.0-beta4 (DI)
- Kermit, slf4j (logging)
- Detekt + ktlint (linting)

## Code Style

- Composable functions use PascalCase (detekt configured to allow this)
- Test names use backtick convention: `` `loading project data updates state` ``
- MIT license header required on all files
