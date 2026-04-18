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
                implementation(project(":feat:projects:fe:common:driving"))
                implementation(project(":feat:projects:fe:app:api"))
            }
        }
        androidMain {
            dependencies {
                implementation(libs.androidx.wear.compose.material)
                implementation(libs.androidx.wear.compose.foundation)
                implementation(libs.koin.core)
                implementation(libs.koin.compose)
                implementation(libs.koin.compose.viewmodel)
                implementation(libs.androidx.lifecycle.runtime.compose)
            }
        }
    }
}
