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
            implementation(project(":feat:sync:fe:app:api"))
            implementation(project(":feat:sync:fe:app:model"))
        }
        commonTest {
            dependencies {
                implementation(project(":feat:inspections:fe:driven:test"))
                implementation(project(":feat:sync:fe:driven:test"))
            }
        }
    }
}
