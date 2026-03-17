# Gradle Convention Plugins

Custom convention plugins are defined in `build-logic/` and registered in `gradle/libs.versions.toml`.
They enforce consistent configuration across modules — targets, dependencies, lint, and inter-module
wiring.

## Plugin hierarchy

There are two independent plugin trees — one for KMP modules and one for JVM-only backend modules.
Each child plugin extends its parent with additional capabilities.

```
snagMultiplatformModule                            Base KMP module
├── snagFrontendMultiplatformModule                + frontend core deps
│   ├── snagDrivingFrontendMultiplatformModule     + Compose UI, navigation, image loading
│   └── snagNetworkFrontendMultiplatformModule     + HTTP client
│       └── snagDrivenFrontendMultiplatformModule  + serialization, database
└── snagContractDrivingBackendMultiplatformModule  + serialization (shared FE/BE contracts)

snagBackendModule                                  Base JVM backend module
├── snagDrivenBackendModule                        + database ORM
└── snagImplDrivingBackendModule                   + HTTP server, serialization
```

## Module path → plugin mapping

The module's location in the project tree determines which plugin to apply:

```
feat/<feature>/
├── business/                → snagMultiplatformModule
├── fe/
│   ├── ports/               → snagFrontendMultiplatformModule
│   ├── app/                 → snagFrontendMultiplatformModule
│   ├── driving/             → snagDrivingFrontendMultiplatformModule
│   └── driven/              → snagDrivenFrontendMultiplatformModule
└── be/
    ├── ports/               → snagBackendModule
    ├── app/                 → snagBackendModule
    ├── driving/
    │   ├── contract/        → snagContractDrivingBackendMultiplatformModule
    │   └── impl/            → snagImplDrivingBackendModule
    └── driven/
        ├── impl/            → snagDrivenBackendModule
        └── test/            → snagBackendModule
```

`lib/` modules follow the same convention based on their layer and platform. `core/` modules use the
base plugins (`snagMultiplatformModule`, `snagFrontendMultiplatformModule`, or `snagBackendModule`)
since the specialized plugins depend on them.

## Auto-wiring

Convention plugins automatically wire most inter-module dependencies. They detect the module's
position in the project tree (via folder-path checks) and add the appropriate sibling/parent module
dependencies.

When creating a new module, start with only the convention plugin applied and no explicit dependencies
in `build.gradle.kts`. Add manual dependencies only for what the build actually requires beyond what
the plugin provides.
