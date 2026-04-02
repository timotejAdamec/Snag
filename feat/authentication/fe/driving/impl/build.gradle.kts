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
            implementation(projects.lib.configuration.common.api)
            implementation(projects.feat.authentication.fe.app.api)
            implementation(project(":lib:routing:common"))
            implementation(project(":lib:network:fe:api"))
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.client.core)
        }
    }
}
