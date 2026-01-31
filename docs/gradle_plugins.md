# Gradle Convention Plugins

Custom convention plugins are defined in `build-logic/` and registered in `gradle/libs.versions.toml`.
They enforce consistent configuration across modules — dependencies, targets, lint, and inter-module wiring.

## Plugin overview

| Alias                                           | Plugin ID                                                         | Platform | Purpose                          | Extends       |
|-------------------------------------------------|-------------------------------------------------------------------|----------|----------------------------------|---------------|
| `snagMultiplatformModule`                       | `libs.plugins.snag.multiplatform.module`                          | All      | Base KMP module                  | —             |
| `snagFrontendMultiplatformModule`               | `libs.plugins.snag.frontend.multiplatform.module`                 | Frontend | Frontend KMP module with logging | Multiplatform |
| `snagDrivingFrontendMultiplatformModule`        | `libs.plugins.snag.driving.frontend.multiplatform.module`         | Frontend | Compose UI screens               | Frontend      |
| `snagNetworkFrontendMultiplatformModule`        | `libs.plugins.snag.network.frontend.multiplatform.module`         | Frontend | HTTP client (Ktor)               | Frontend      |
| `snagDrivenFrontendMultiplatformModule`         | `libs.plugins.snag.driven.frontend.multiplatform.module`          | Frontend | Data/adapter implementations     | Network       |
| `snagBackendModule`                             | `libs.plugins.snag.backend.module`                                | Backend  | JVM-only backend module          | —             |
| `snagImplDrivingBackendModule`                  | `libs.plugins.snag.impl.driving.backend.module`                   | Backend  | Ktor HTTP routes                 | Backend       |
| `snagContractDrivingBackendMultiplatformModule` | `libs.plugins.snag.contract.driving.backend.multiplatform.module` | Shared   | FE/BE API contracts              | Multiplatform |

## Plugin inheritance

```
snagMultiplatformModule
├── snagFrontendMultiplatformModule
│   ├── snagDrivingFrontendMultiplatformModule    (+ Compose, Navigation, Coil)
│   └── snagNetworkFrontendMultiplatformModule    (+ Ktor client)
│       └── snagDrivenFrontendMultiplatformModule (+ Serialization, DB)
└── snagContractDrivingBackendMultiplatformModule (+ Serialization)

snagBackendModule
└── snagImplDrivingBackendModule                  (+ Ktor server, Serialization)
```

## Plugin details

### snagMultiplatformModule

**Source:** `plugins/MultiplatformModulePlugin.kt` + `configuration/MultiplatformModuleSetup.kt`

Applies: Kotlin Multiplatform, Android KMP Library, KSP.

Configures:
- Targets: Android, iOS (arm64 + simulator), JVM, JS, WasmJS
- Custom `nonWebMain` source set grouping Android, iOS, JVM
- Common dependencies: coroutines, Koin, immutable collections
- Koin KSP compiler on all KSP configurations
- Automatic inter-module dependency wiring (ports → business, app → ports, etc.)
- Lint: detekt + ktlint (via `LintSetup.kt`)
- Android namespace: `cz.adamec.timotej.snag.<module-path>`

### snagFrontendMultiplatformModule

**Source:** `plugins/FrontendMultiplatformModulePlugin.kt` + `configuration/FrontendMultiplatformModuleSetup.kt`

Adds on top of Multiplatform:
- Kermit logging + Koin integration
- `:lib:core:fe` dependency

### snagDrivingFrontendMultiplatformModule

**Source:** `plugins/DrivingFrontendMultiplatformModulePlugin.kt` + `configuration/ComposeMultiplatformModuleSetup.kt`

Applies: Compose Multiplatform, Compose Compiler, Compose Hot Reload, Kotlin Serialization.

Adds on top of Frontend:
- Compose runtime, foundation, Material3, UI, layouts
- Lifecycle + Navigation integrations (Navigation3, adaptive)
- Koin Compose + ViewModel integration
- Coil image loading with Ktor
- Platform-specific: Activity Compose (Android), Swing coroutines (JVM), browser navigation (Web)
- Experimental Material3 APIs enabled

### snagNetworkFrontendMultiplatformModule

**Source:** `plugins/NetworkFrontendMultiplatformModulePlugin.kt` + `configuration/NetworkMultiplatformModuleSetup.kt`

Adds on top of Frontend:
- Ktor client core, content negotiation, logging
- Kotlin serialization JSON
- Platform engines: OkHttp (Android/JVM), Darwin (iOS), CIO (WasmJS), JS (JS)
- `:lib:network:fe` dependency

### snagDrivenFrontendMultiplatformModule

**Source:** `plugins/DrivenFrontendMultiplatformModulePlugin.kt` + `configuration/StoreMultiplatformModuleSetup.kt`

Applies: Kotlin Serialization.

Adds on top of Network:
- `:feat:shared:database:fe` dependency

### snagBackendModule

**Source:** `plugins/BackendModulePlugin.kt` + `configuration/BackendModuleSetup.kt`

Applies: Kotlin JVM.

Configures:
- `:lib:core:be` dependency
- Coroutines, Koin core, SLF4J logging
- Test: JUnit, coroutines test
- Automatic inter-module dependency wiring (same rules as Multiplatform)
- Lint: detekt + ktlint

### snagImplDrivingBackendModule

**Source:** `plugins/ImplDrivingBackendModulePlugin.kt` + `configuration/KtorBackendModuleSetup.kt`

Applies: Ktor plugin, Kotlin Serialization.

Adds on top of Backend:
- Ktor server core, Koin Ktor integration
- Kotlin serialization JSON
- Ktor server test host (test)
- `:lib:routing:be` dependency

### snagContractDrivingBackendMultiplatformModule

**Source:** `plugins/ContractDrivingBackendMultiplatformModulePlugin.kt` + `configuration/ContractModuleSetup.kt`

Applies: Kotlin Serialization.

Adds on top of Multiplatform:
- `:lib:core:common` dependency
- Kotlin serialization core
- Ktor serialization JSON

## Module path → plugin

For feature modules at `feat/<feature>/`, the path determines the plugin:

```
feat/<feature>/
├── business/                → snagMultiplatformModule
├── fe/
│   ├── ports/               → snagFrontendMultiplatformModule
│   ├── app/
│   │   ├── api/             → snagFrontendMultiplatformModule
│   │   └── impl/            → snagFrontendMultiplatformModule
│   ├── driving/
│   │   ├── api/             → snagDrivingFrontendMultiplatformModule
│   │   └── impl/            → snagDrivingFrontendMultiplatformModule
│   └── driven/
│       ├── impl/            → snagDrivenFrontendMultiplatformModule
│       └── test/            → snagDrivenFrontendMultiplatformModule
└── be/
    ├── ports/               → snagBackendModule
    ├── app/
    │   ├── api/             → snagBackendModule
    │   └── impl/            → snagBackendModule
    ├── driving/
    │   ├── contract/        → snagContractDrivingBackendMultiplatformModule
    │   └── impl/            → snagImplDrivingBackendModule
    └── driven/
        ├── impl/            → snagBackendModule
        └── test/            → snagBackendModule
```

Library modules (`lib/`) follow the same convention based on their layer and platform
(`fe/`, `be/`, or top-level for shared). `core` modules are an exception — they use the
base `snagMultiplatformModule` or `snagBackendModule` since other plugins depend on them.
