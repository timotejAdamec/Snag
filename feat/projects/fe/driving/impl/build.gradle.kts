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
    alias(libs.plugins.snagDrivingFrontendMultiplatformModule)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(project(":feat:projects:fe:app:api"))
                implementation(project(":feat:projects:business:model"))
                api(project(":feat:structures:fe:app:api"))
                api(project(":feat:clients:fe:app:api"))
                api(project(":feat:inspections:fe:app:api"))
                api(project(":feat:reports:fe:app:api"))
                api(project(":feat:users:fe:app:api"))
            }
        }
        commonTest {
            dependencies {
                implementation(project(":feat:inspections:fe:driven:test"))
                implementation(project(":feat:projects:fe:driven:test"))
                implementation(project(":feat:clients:fe:driven:test"))
                implementation(project(":feat:reports:fe:driven:test"))
                implementation(project(":feat:structures:fe:driven:test"))
                implementation(project(":feat:sync:fe:driven:test"))
                implementation(project(":feat:users:fe:driven:test"))
                implementation(project(":lib:network:fe:test"))
            }
        }
    }
}
