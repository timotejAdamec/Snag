/*
 * Copyright (c) 2026 Timotej Adamec
 * SPDX-License-Identifier: MIT
 *
 * This file is part of the thesis:
 * "Multiplatform snagging system with code sharing maximisation"
 *
 * Czech Technical University in Prague
 * Faculty of Information Technology
 * Department of Software Engineering
 */

rootProject.name = "Snag"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

includeBuild("build-logic")
include(":androidApp")
include(":composeApp")
include(":koinModulesAggregate:fe")
include(":koinModulesAggregate:be")
include(":testInfra:common")
include(":testInfra:fe")
include(":testInfra:be")
include(":server:api")
include(":server:impl")
include(":feat:projects:business")
include(":feat:projects:fe:app:api")
include(":feat:projects:fe:app:impl")
include(":feat:projects:fe:ports")
include(":feat:projects:fe:driven:impl")
include(":feat:projects:be:driving:contract")
include(":feat:projects:be:driving:impl")
include(":feat:projects:be:driven:impl")
include(":feat:projects:be:app:api")
include(":feat:projects:be:app:impl")
include(":feat:projects:be:ports")
include(":feat:projects:be:model")
include(":feat:projects:fe:model")
include(":feat:projects:fe:driving:api")
include(":feat:projects:fe:driving:impl")
include(":feat:shared:database:fe:api")
include(":feat:shared:database:fe:impl")
include(":feat:shared:database:fe:test")
include(":feat:shared:database:be:impl")
include(":feat:shared:database:be:test")
include(":feat:shared:rules:business:api")
include(":feat:shared:rules:business:impl")
include(":lib:design:fe")
include(":lib:routing:fe")
include(":lib:routing:be")
include(":lib:configuration:common:api")
include(":lib:configuration:be:api")
include(":lib:configuration:be:impl")
include(":lib:network:fe:model")
include(":lib:network:fe:app:api")
include(":lib:network:fe:app:impl")
include(":lib:network:fe:ports")
include(":lib:network:fe:driven:impl")
include(":lib:network:fe:driven:test")
include(":feat:structures:be:driving:contract")
include(":feat:structures:be:driving:impl")
include(":lib:network:be:test")
include(":feat:projects:fe:driven:test")
include(":lib:core:be")
include(":lib:core:fe")
include(":lib:core:common")
include(":feat:findings:business")
include(":feat:findings:fe:app:api")
include(":feat:findings:fe:app:impl")
include(":feat:findings:fe:driving:api")
include(":feat:findings:fe:driving:impl")
include(":feat:findings:fe:ports")
include(":feat:findings:fe:driven:impl")
include(":feat:findings:fe:driven:test")
include(":feat:findings:be:driving:contract")
include(":feat:findings:be:driving:impl")
include(":feat:findings:be:driven:impl")
include(":feat:findings:be:app:api")
include(":feat:findings:be:app:impl")
include(":feat:findings:be:ports")
include(":feat:findings:be:model")
include(":feat:findings:fe:model")
include(":feat:structures:business")
include(":feat:structures:be:app:api")
include(":feat:structures:be:app:impl")
include(":feat:structures:be:driven:impl")
include(":feat:structures:be:ports")
include(":feat:structures:be:model")
include(":feat:structures:fe:driving:api")
include(":feat:structures:fe:driving:impl")
include(":feat:structures:fe:app:api")
include(":feat:structures:fe:app:impl")
include(":feat:structures:fe:ports")
include(":feat:structures:fe:driven:impl")
include(":feat:structures:fe:driven:test")
include(":lib:sync:fe:ports")
include(":lib:sync:fe:app:api")
include(":lib:sync:fe:app:impl")
include(":lib:sync:fe:driven:impl")
include(":lib:sync:fe:driven:test")
include(":lib:sync:fe:model")
include(":lib:database:fe")
include(":lib:database:fe:test")
include(":feat:structures:fe:model")
include(":feat:clients:business")
include(":feat:clients:be:model")
include(":feat:clients:be:app:api")
include(":feat:clients:be:app:impl")
include(":feat:clients:be:ports")
include(":feat:clients:be:driven:impl")
include(":feat:clients:be:driving:contract")
include(":feat:clients:be:driving:impl")
include(":feat:clients:fe:model")
include(":feat:clients:fe:app:api")
include(":feat:clients:fe:app:impl")
include(":feat:clients:fe:ports")
include(":feat:clients:fe:driven:impl")
include(":feat:clients:fe:driven:test")
include(":feat:clients:fe:driving:api")
include(":feat:clients:fe:driving:impl")
include(":feat:inspections:business")
include(":feat:inspections:fe:app:api")
include(":feat:inspections:fe:app:impl")
include(":feat:inspections:fe:ports")
include(":feat:inspections:fe:driven:impl")
include(":feat:inspections:fe:driven:test")
include(":feat:inspections:fe:model")
include(":feat:inspections:fe:driving:api")
include(":feat:inspections:fe:driving:impl")
include(":feat:inspections:be:app:api")
include(":feat:inspections:be:app:impl")
include(":feat:inspections:be:ports")
include(":feat:inspections:be:model")
include(":feat:inspections:be:driven:impl")
include(":feat:inspections:be:driving:contract")
include(":feat:inspections:be:driving:impl")
include(":lib:storage:be:api")
include(":lib:storage:be:impl")
include(":lib:storage:be:test")
include(":lib:storage:contract")
include(":lib:storage:fe:api")
include(":lib:storage:fe:impl")
include(":lib:storage:fe:test")
include(":feat:shared:storage:be")
include(":feat:shared:storage:fe")
