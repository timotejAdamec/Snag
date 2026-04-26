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
        commonMain.dependencies {
            implementation(projects.lib.design.fe.api)
            implementation(projects.lib.configuration.fe.api)
        }
        jvmMain.dependencies {
            implementation(projects.lib.storage.fe.api)
            implementation(libs.ktor.client.okhttp)
        }
    }
}
