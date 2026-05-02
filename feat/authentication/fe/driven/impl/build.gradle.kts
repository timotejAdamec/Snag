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
            implementation(libs.oidc.ktor)
        }
        jvmMain.dependencies {
            implementation(projects.lib.storage.fe.api)
        }
        val webMain by getting {
            dependencies {
                implementation(libs.kotlinx.browser)
                // oidc-appsupport's JS variant lists oidc-preferences only in runtimeElements,
                // not apiElements — must declare it explicitly to compile against Preferences*.
                implementation(libs.oidc.preferences)
            }
        }
    }
}
