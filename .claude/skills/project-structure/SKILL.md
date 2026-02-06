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

- **`business/`** — Platform-agnostic domain models. Innermost core. Only depends on `:lib:core`.
- **`be/`** — Backend: `app/` (use cases) > `ports/` (interfaces) > `driven/` (DB implementations) + `driving/` (HTTP routes)
- **`fe/`** — Frontend: `app/` (use cases) > `ports/` (interfaces) > `driven/` (HTTP/SQLite implementations) + `driving/` (Compose screens)

### Module splits

- `api/` or `contract/` — Public interfaces (typical for `app/` and `driving/`)
- `impl/` — Production implementations
- `test/` — Fake/in-memory implementations for unit tests
  - `driven/test/` — Fakes for port interfaces (DB, API)
  - `app/test/` — Fakes for use case interfaces (cross-feature testing)

### Library modules (`lib/`)

Domain-agnostic reusable code used by `driven` and `driving` feature modules.
`core` modules can be used anywhere.

### Dependency direction

`driving` -> `app` -> `ports` -> `model` -> `business`
`driven` -> `app` -> `model` -> `business`
