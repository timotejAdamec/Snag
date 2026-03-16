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

- **`business/model/`** — Platform-agnostic domain model interfaces. Innermost core. Only depends on `:lib:core`.
- **`app/model/`** — Shared application models extending `business/model` with `Versioned` (`updatedAt`). Used by both FE and BE.
- **`be/`** — Backend: `app/model/` (extends shared `app/model` with `SoftDeletable`) > `app/` (use cases) > `ports/` (interfaces) > `driven/` (DB implementations) + `driving/` (HTTP routes)
- **`fe/`** — Frontend: `app/` (use cases) > `ports/` (interfaces) > `driven/` (HTTP/SQLite implementations) + `driving/` (Compose screens)

### Module splits

- `api/` or `contract/` — Public interfaces or cross-feature-accessible code. Can appear in any layer.
  `contract/` is a special variant for shared FE/BE DTOs (driving layer).
- `impl/` — Production implementations
- `test/` — Fake/in-memory implementations for unit tests (typical for `driven/` layer)

### Shared modules (`feat/shared/`)

- **`shared/database/`** — Shared database infrastructure
- **`shared/rules/business/`** — Cross-cutting business rules (`api/` + `impl/`). Auto-wired to all feature modules.

### Library modules (`lib/`)

Feature-agnostic reusable code (not tied to a specific domain area). Two kinds:
- **Infrastructural** (e.g., network, database, design, routing) — purely adapter/connector role.
  Can be a single top-level module or an api/impl/test split. Implicitly living in the adapters layer
  (to be used by `driven` and `driving` feature modules or other such `lib` modules).
- **Cross-cutting domain logic** (e.g., sync) — structured the same as `feat` modules
  (app/api, app/impl, ports, driven, etc.).

`core` modules can be used anywhere.

### Dependency management

Most dependencies are auto-wired via convention plugins. When creating a new module, start with only the convention plugin applied — add explicit dependencies only if the build requires them.

#### Dependency direction

`driving` -> `app` -> `ports` -> `app/model` -> `business/model`
`driven` -> `ports` -> `app/model` -> `business/model`
`be/app/model` -> `app/model` -> `business/model`

#### Cross feature/lib dependencies

When a feature needs a dependency on another feature or lib or a lib needs to depend on another lib

