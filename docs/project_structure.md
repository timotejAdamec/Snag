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
- `business` for code that is shared by all platforms including the server.
  - This code is in this sense platform-agnostic.
  - This module is the core from clean architecture perspective.
  - This module has no dependencies on other modules except the `:lib:core`.
- `be` for backend code.
- `fe` for frontend code.

The `be` and `fe` directories are platform-specific. They are broken down into layer directories/modules:
- `app` for application domain code. This is a core layer that sits around the platform-agnostic
`business` module.
- `ports` as in the ports and adapters pattern. These ports are used by `app` modules.
- `driven` as in the driven ports pattern. This is the most outer layer. These ports are
implementations of the `ports`. They implement different technologies to satisfy the `ports` API.
- `driving` as in the driving adapters pattern. This is also the most outer layer. This code also
integrates with technologies. This code depends on the `app` module for application logic.
`driving` code is the entry point for the platform.

#### Feature module splits
Each feature module/directory can be split into submodules if there is a need for it:
- `api`/`contract`
  - `contract` is a special api code that is used as a contract between the frontend and backend but
    is in the `driving` adapter layer, not in the core `business` layer.
- `impl` - contains production implementations for use in production.
  - Depends on `api`/`contract`.
- `test` - contains in-memory and other non-production unit-test-friendly implementations for use
  instead of `impl`.
    - Depends on `api`/`contract`.

The `api` and `impl` split is typical for frontend driving code.
The `test` and `impl` split is typical for driven code so that tests in `driving` and `app` layers
can run with non-production unit-test-friendly adapter `ports`.
The `contract` and `impl` split is typical for backend driving code.

## Library modules
Library modules are located in the `lib` directory. These contain "domain-agnostic" code.
"Domain-agnostic" means it can theoretically be used in other projects.

They can be structured into submodules just as features. However, if the library is only part of
infrastructure, so integrating with technologies – a driven adapter – then the top level directory
can be just a simple module, or divided into `fe`, `be` or `common`, depending on the semantics of
the module. The only exception are the `core` modules, which can be used in any layer.

## Modules configurations
The project uses custom plugins located in the top-level `build-logic` module.
