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
                implementation(project(":feat:inspections:fe:app:api"))
                implementation(project(":feat:projects:fe:app:api"))
                implementation(project(":feat:projects:fe:driving:api"))
                implementation(libs.kotlinx.datetime)
            }
        }
        commonTest {
            dependencies {
                implementation(project(":feat:inspections:fe:driven:test"))
                implementation(project(":feat:projects:fe:app:impl"))
                implementation(project(":feat:projects:fe:driven:test"))
                implementation(project(":lib:sync:fe:driven:test"))
            }
        }
    }
}
