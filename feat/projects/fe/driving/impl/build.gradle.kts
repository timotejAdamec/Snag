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
    alias(libs.plugins.snagDrivingMultiplatformModule)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":lib:design:fe"))
                implementation(project(":feat:projects:fe:driving:api"))
                implementation(project(":feat:projects:fe:app"))
                implementation(project(":feat:projects:business"))
            }
        }
    }
}
