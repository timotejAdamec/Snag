# Project Structure

Module structure with architecture layers and multiplatform levels.

## Application modules
Each platform application is runnable from the following modules:
- :composeApp
  - jvm, js, wasmJs
- :androidApp
- :iosApp
- :server

## Feature modules
Feature modules are located in the `feat` directory. These contain domain-tied code. Each feature is
a submodule there with an exception to the `shared` directory, which contains concerns that are shared
between the features. If no layer is specified, the module is considered to be in the infrastructure
(`driving`/`driven`) layer.

Can depend on both `core` and `feat` code.

### Feature directory structure
Each feature is structured by platforms and by layers:
- `business/` is a directory (not a module) containing platform-agnostic domain code:
  - `business/model/` — Pure domain interfaces representing the analytical domain model. These have
    no dependencies on other modules except `:core:foundation`. No `updatedAt` or sync concerns.
  - `business/rules/` — Business rules operating on domain interfaces (e.g., validation rules).
    Depends on `business/model/`.
- `app/` contains application-layer code shared by all platforms:
  - `app/model/` — Shared application interfaces (`AppX : X, Versioned`) and data classes. Extends
    business models with `updatedAt` for sync versioning.
- `be/` for backend code.
- `fe/` for frontend code.

The `be/` and `fe/` directories are platform-specific. They are broken down into layer
directories/modules:
- `app/` for application domain code. This is a core layer that sits around the platform-agnostic
  `app/` and `business/` modules.
  - `app/model/` extends shared `app/model/` with platform-specific data.
- `ports/` as in the ports and adapters pattern. These ports are used by `app/` modules.
- `driven/` as in the driven ports pattern. This is the most outer layer. These ports are
  implementations of the `ports/`. They implement different technologies to satisfy the `ports/` API.
- `driving/` as in the driving adapters pattern. This is also the most outer layer. This code also
  integrates with technologies. This code depends on the `app/` module for application logic.
  `driving/` code is the entry point for the platform.

### Shared modules (`feat/shared/`)
The `shared` directory contains cross-cutting concerns used by multiple features.

### Cross-feature dependencies
Dependencies between features and libs is handled by accessing:
- use cases (`app/api`) for business logic.
- `driving/api` for navigation or UI components.

## Core modules
Core modules are located in the `core` directory. These contain **infrastructure-free,
feature-agnostic** domain types, language extensions, and utilities shared across all modules. They
have no dependencies on external technologies.

Cannot depend on `feat` or `lib` code.

## Library modules
Library modules are located in the `lib` directory. These contain **feature-agnostic infrastructure
integrations** — external framework wrappers and connectors not tied to a specific domain area. All
library modules are purely infrastructural (connector/adapter role). Their code lives implicitly in
the adapters layer; for such modules architecture layering can be omitted and the code is understood
to be driven/driving.

Can depend on `core` code, cannot depend on `feat` code.

Notable library module families:
- `lib/network/` — Ktor HTTP integration. `common/` holds shared serialization config,
  `fe/{api,impl,test}` wraps the Ktor HTTP client, `be/{api,impl,test}` wraps the Ktor HTTP server
  (routing, CORS, content negotiation, logging, status pages).
- `lib/configuration/` — Build-time and runtime config values (versions, auth credentials, server
  targets, environment variables).

#### Encapsulation module splits
Each feature/core/library module/directory can be split into submodules if there is a need for it:
- `api` — Public interfaces. Can appear in any layer.
    - `contract` is a special variant of `api` used as a shared FE/BE contract in the `driving`
      adapter layer (not in the core `business` layer).
- `impl` — Production implementations. Depends on `api`/`contract`.
- `test` — In-memory and other non-production unit-test-friendly implementations for use instead
  of or adjusting `impl`. In features, it is typical for the `driven` layer so that tests in `driving` and `app`
  layers can run with non-production unit-test-friendly adapter `ports`.

## Dependency directions
```
[application] -> feat -> lib -> core

driving -
         -> app -> business
driven  -

impl -> api
test -> (impl) -> api
```

## Modules configurations
The project uses custom plugins located in the top-level `build-logic` module.
Convention plugins auto-wire most inter-module dependencies — when creating a new module, apply only
the convention plugin and add explicit dependencies only if the build requires them.

## Local project structure hints
`README.md` files in the codebase also give local project structure information.
