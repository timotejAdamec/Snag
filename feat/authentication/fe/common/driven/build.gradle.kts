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
    alias(libs.plugins.snagDrivenFrontendMultiplatformModule)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.lib.configuration.fe.api)
            implementation(libs.oidc.appsupport)
            implementation(libs.oidc.tokenstore)
        }
        val webMain by getting {
            dependencies {
                implementation(libs.kotlinx.browser)
            }
        }
    }
}
