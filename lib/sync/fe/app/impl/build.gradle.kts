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
            api(project(":lib:sync:fe:app:api"))
            implementation(project(":lib:network:fe"))
        }
        commonTest.dependencies {
            implementation(project(":lib:sync:fe:driven:test"))
            implementation(project(":lib:network:fe:test"))
            implementation(project(":feat:inspections:fe:driven:test"))
        }
    }
}
