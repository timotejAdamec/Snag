---
name: project-structure
description: KMP & plain JVM module structure with Clean Architecture + Hexagonal (Ports & Adapters) layers. Use when navigating the codebase, deciding where new code belongs, creating new modules, or answering questions about module layout and dependencies.
user-invocable: false
allowed-tools: Read, Glob, Grep
---

# Project Structure

Read [docs/project_structure.md](../../../docs/project_structure.md) for the full module layout documentation before answering questions about where code belongs or how modules are organized.

## Quick reference

### Application entry points

| Module | Platform |
|---|---|
| `:composeApp` | Desktop (jvm), Web (js, wasmJs) |
| `:androidApp` | Android |
| `:iosApp` | iOS |
| `:server:impl` | Backend (Ktor) |

### Feature modules (`feat/<feature>/`)

Each feature follows hexagonal architecture with these layers:

- **`business/`** — Platform-agnostic domain models. Innermost core. Only depends on `:core:foundation`.
- **`be/`** — Backend: `app/` (use cases) > `ports/` (interfaces) > `driven/` (DB implementations) + `driving/` (HTTP routes)
- **`fe/`** — Frontend: `app/` (use cases) > `ports/` (interfaces) > `driven/` (HTTP/SQLite implementations) + `driving/` (Compose screens)

### Module splits

- `api/` or `contract/` — Public interfaces or cross-feature-accessible code. Can appear in any layer.
  `contract/` is a special variant for shared FE/BE DTOs (driving layer).
- `impl/` — Production implementations
- `test/` — Fake/in-memory implementations for unit tests (typical for `driven/` layer)

### Shared modules (`feat/shared/`)

- **`shared/database/`** — Shared database infrastructure
- **`shared/rules/business/`** — Cross-cutting business rules (`api/` + `impl/`). Auto-wired to all feature modules.

### Core modules (`core/`)

Infrastructure-free, feature-agnostic domain types and utilities shared across all modules:
- **`foundation/`** — App-wide primitives (`Timestamp`, `UuidProvider`, `ApplicationScope`, `Initializer`). Submodules: `common/`, `fe/`, `be/`.
- **`network/fe`** — Network data lifecycle domain types (`OnlineDataResult`, `SafeApiCall`, `NetworkException`, `ConnectionStatusProvider`). No Ktor dependency.

### Library modules (`lib/`)

Feature-agnostic **infrastructure integrations** — external framework wrappers and connectors:
network (Ktor HTTP client), database (SQLDelight), storage (GCS), design (Compose UI), routing
(navigation), configuration (build/server config). Purely adapter/connector role, implicitly in
the adapters layer.

### Dependency management

Most dependencies are auto-wired via convention plugins. When creating a new module, start with only the convention plugin applied — add explicit dependencies only if the build requires them.

#### Dependency direction

`driving` -> `app` -> `ports` -> `model` -> `business`
`driven` -> `app` -> `model` -> `business`

#### Cross feature/lib dependencies

When a feature needs a dependency on another feature or lib or a lib needs to depend on another lib

