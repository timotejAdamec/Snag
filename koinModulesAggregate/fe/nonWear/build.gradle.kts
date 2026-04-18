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

plugins {
    alias(libs.plugins.snagFrontendMultiplatformModule)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Common aggregate provides truly-shared wiring; the non-Wear
            // aggregate layers phone/iOS/web/JVM variants on top.
            api(projects.koinModulesAggregate.fe)
            implementation(projects.feat.projects.fe.driving.nonWear)
        }
        androidMain.dependencies {
            implementation(projects.feat.authentication.fe.driven.nonWear)
        }
    }
}
