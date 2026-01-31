---
name: gradle-plugins
description: Custom Gradle convention plugins and which modules use them. Use when creating new modules, modifying build.gradle.kts files, or choosing the right plugin for a module's layer and platform.
user-invocable: false
allowed-tools: Read, Glob, Grep
---

# Gradle Convention Plugins

Read [docs/gradle_plugins.md](../../../docs/gradle_plugins.md) for full plugin details, dependencies each plugin adds, and the complete module-to-plugin mapping.

## Module path → plugin

When creating a new module at `feat/<feature>/<path>`, use this mapping:

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

Applied in `build.gradle.kts` as: `alias(libs.plugins.<alias>)`

## Plugin inheritance chain

```
snagMultiplatformModule
├── snagFrontendMultiplatformModule
│   ├── snagDrivingFrontendMultiplatformModule    (+ Compose)
│   └── snagNetworkFrontendMultiplatformModule    (+ Ktor client)
│       └── snagDrivenFrontendMultiplatformModule (+ Serialization, DB)
└── snagContractDrivingBackendMultiplatformModule (+ Serialization)

snagBackendModule
└── snagImplDrivingBackendModule                  (+ Ktor server)
```

Applied in `build.gradle.kts` as: `alias(libs.plugins.<alias>)`

Plugin sources: `build-logic/src/main/kotlin/cz/adamec/timotej/snag/buildsrc/plugins/`
Configuration sources: `build-logic/src/main/kotlin/cz/adamec/timotej/snag/buildsrc/configuration/`
