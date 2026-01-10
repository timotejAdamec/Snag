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
include(":server")
include(":feat:projects:business")
include(":feat:projects:fe:app")
include(":feat:projects:fe:ports")
include(":feat:projects:fe:driven")
include(":feat:projects:be:driving:contract")
include(":feat:projects:be:driving:impl")
include(":feat:projects:be:driven")
include(":feat:projects:be:app")
include(":feat:projects:be:ports")
include(":feat:projects:fe:driving:api")
include(":feat:projects:fe:driving:impl")
include(":feat:shared:database:fe")
include(":lib:design:fe")
include(":lib:routing:fe")
include(":lib:core")
include(":lib:routing:be")
include(":feat:structures:be:driving:contract")
include(":feat:structures:be:driving:impl")
include(":lib:network:fe")
