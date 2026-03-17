# Issue #80: Revisit Libs Architecture — Execution Summary

## What changed

### 1. `lib/core` → `core/foundation/`
- Moved `lib/core/{common,fe,be}` to `core/foundation/{common,fe,be}`
- Package rename: `cz.adamec.timotej.snag.lib.core.*` → `cz.adamec.timotej.snag.core.foundation.*`
- Convention plugins updated to auto-wire `:core:foundation:*` instead of `:lib:core:*`

### 2. Created `core/network/fe`
- Moved from `lib/network/fe`: `SafeApiCall`, `NetworkException`, `Mapper`, `NetworkExceptionLog`, `LH`, `ConnectionStatusProvider`
- Moved from `core/foundation/fe`: `OnlineDataResult`, `OfflineFirstDataResult`, `OfflineFirstUpdateDataResult`
- Package: `cz.adamec.timotej.snag.core.network.fe`
- Deleted `InternetConnectionStatusListener` (unnecessary indirection over `ConnectionStatusProvider`)
- Deleted `lib/network/fe/model` module entirely
- Convention plugin auto-wires `:core:network:fe` to all FE modules

### 3. `lib/sync` → `feat/sync`
- Moved `lib/sync/{fe,be}/*` to `feat/sync/{fe,be}/*`
- Package rename: `cz.adamec.timotej.snag.lib.sync.*` → `cz.adamec.timotej.snag.sync.*`
- Convention plugin updated: `:feat:sync:be:api` auto-wired to all BE modules

## New top-level structure
```
core/                      Infrastructure-free domain types, language extensions, utilities
├── foundation/            App-wide primitives (Timestamp, UuidProvider, ApplicationScope, Initializer)
│   ├── common/
│   ├── fe/
│   └── be/
└── network/
    └── fe/                Network-dependent data lifecycle domain types

lib/                       Infrastructure integrations (external framework wrappers)
├── network/               Ktor HTTP client infrastructure only
├── database/              SQLDelight integration
├── storage/               GCS / file upload
├── design/                Compose UI design system
├── routing/               Navigation
└── configuration/         Build config, Ktor server config

feat/                      Features with full hexagonal architecture
├── projects/
├── findings/
├── structures/
├── inspections/
├── clients/
├── users/
├── reports/
├── sync/                  ← moved from lib/sync
└── shared/
```
