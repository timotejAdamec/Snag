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
include(":feat:projects:be:driven:test")
include(":feat:projects:be:app:api")
include(":feat:projects:be:app:impl")
include(":feat:projects:be:ports")
include(":feat:projects:be:model")
include(":feat:projects:fe:model")
include(":feat:projects:fe:driving:api")
include(":feat:projects:fe:driving:impl")
include(":feat:shared:database:fe")
include(":lib:design:fe")
include(":lib:routing:fe")
include(":lib:routing:be")
include(":lib:configuration:be:api")
include(":lib:configuration:be:impl")
include(":feat:structures:be:driving:contract")
include(":feat:structures:be:driving:impl")
include(":lib:network:fe")
include(":feat:projects:fe:driven:test")
include(":lib:core:be")
include(":lib:core:fe")
include(":lib:core:common")
include(":feat:findings:business")
include(":feat:findings:fe:app:api")
include(":feat:findings:fe:app:impl")
include(":feat:findings:fe:app:test")
include(":feat:findings:fe:driving:api")
include(":feat:findings:fe:driving:impl")
include(":feat:findings:fe:ports")
include(":feat:findings:fe:driven:impl")
include(":feat:findings:fe:driven:test")
include(":feat:findings:be:driving:contract")
include(":feat:findings:be:driving:impl")
include(":feat:findings:be:driven:impl")
include(":feat:findings:be:driven:test")
include(":feat:findings:be:app:api")
include(":feat:findings:be:app:impl")
include(":feat:findings:be:ports")
include(":feat:findings:be:model")
include(":feat:findings:fe:model")
include(":feat:structures:business")
include(":feat:structures:be:app:api")
include(":feat:structures:be:app:impl")
include(":feat:structures:be:driven:impl")
include(":feat:structures:be:driven:test")
include(":feat:structures:be:ports")
include(":feat:structures:be:model")
include(":feat:structures:fe:driving:api")
include(":feat:structures:fe:driving:impl")
include(":feat:structures:fe:app:api")
include(":feat:structures:fe:app:impl")
include(":feat:structures:fe:app:test")
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
include(":feat:structures:fe:model")
