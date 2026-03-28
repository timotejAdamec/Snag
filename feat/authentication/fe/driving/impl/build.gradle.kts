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
            implementation(projects.lib.network.fe.api)
            implementation(projects.lib.routing.common)
            implementation(projects.feat.authentication.fe.app.api)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.auth)
        }
    }
}
