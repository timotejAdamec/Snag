# Project Structure

## Application modules
Each platform application is runnable from the following modules:
- :composeApp
  - jvm, js, wasmJs
- :androidApp
- :iosApp
- :server:impl

## Feature directories
Feature modules are located in the `feat` directory. These contain domain-tied code. Each feature is
a submodule there with an exception of the `shared` directory, which contains concerns that are shared
between the features. If no layer is specified, the module is considered to be in the `driven` layer.

### Feature directory structure
Each feature contains is structured by platforms and by layers:
- `business/model` for domain model interfaces that are shared by all platforms including the server.
  - This code is in this sense platform-agnostic.
  - This module is the core from clean architecture perspective.
  - Business entities are interfaces (pure domain contracts, no sync/versioning metadata).
  - This module has no dependencies on other modules except the `:lib:core`.
- `app/model` for shared application model interfaces and data classes extending `business/model`
  with `Versioned` (adds `updatedAt` timestamp). Used by both `fe` and `be` code.
- `be` for backend code.
- `fe` for frontend code.

The `be` and `fe` directories are platform-specific. They are broken down into layer directories/modules:
- `app` for application domain code. This is a core layer that sits around the platform-agnostic
`business` module.
- `model` (under `be/`) allows extending the models in the `app/model` module with BE-specific
data (e.g., `SoftDeletable` for soft-delete support). Used as a dependency for all the other
BE-specific layers.
- `ports` as in the ports and adapters pattern. These ports are used by `app` modules.
- `driven` as in the driven ports pattern. This is the most outer layer. These ports are
implementations of the `ports`. They implement different technologies to satisfy the `ports` API.
- `driving` as in the driving adapters pattern. This is also the most outer layer. This code also
integrates with technologies. This code depends on the `app` module for application logic.
`driving` code is the entry point for the platform.

#### Feature module splits
Each feature module/directory can be split into submodules if there is a need for it:
- `api` — Public interfaces or cross-feature-accessible code. Can appear in any layer.
  - `contract` is a special variant of `api` used as a shared FE/BE contract in the `driving`
    adapter layer (not in the core `business` layer).
- `impl` — Production implementations. Depends on `api`/`contract`.
- `test` — In-memory and other non-production unit-test-friendly implementations for use instead
  of `impl`. Typical for the `driven` layer so that tests in `driving` and `app` layers can run with
  non-production unit-test-friendly adapter `ports`.

### Shared modules (`feat/shared/`)

The `shared` directory contains cross-cutting concerns used by multiple features:
- `database/` — Shared database infrastructure (`fe/`, `be/impl`, `be/test`).
- `rules/business/` — Cross-cutting business-layer validation rules (`api/impl` split).
  Auto-wired as a dependency to all feature modules via the convention plugin.

### Cross-feature dependencies

Dependencies between features and libs is handled by accessing:
- use cases (`app/api`) for business logic.
- `driving/api` for navigation or UI components.

## Library modules
Library modules are located in the `lib` directory. These contain **feature-agnostic** code –
functionality not tied to a specific domain area (feature) that can theoretically be reused in other
projects.

Some library modules are purely infrastructural (connector/adapter role) – e.g., network, database,
design system, routing. These integrate with technologies and their code lives implicitly in the
adapters layer. For such modules architecture layering can be omitted and the code is understood to
be driven/driving.

Other library modules contain cross-cutting domain logic shared by multiple features – e.g., sync.
These are structured into submodules just as features (app/api, app/impl, ports, driven, etc.).

The only exception are the `core` modules, which can be used in any layer.

## Modules configurations
The project uses custom plugins located in the top-level `build-logic` module.
Convention plugins auto-wire most inter-module dependencies — when creating a new module, apply only
the convention plugin and add explicit dependencies only if the build requires them.
