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
            implementation(project(":feat:projects:be:driving:contract"))
            implementation(project(":feat:projects:business"))
            implementation(project(":lib:sync:fe:app:api"))
            implementation(project(":lib:sync:fe:model"))
            implementation(project(":lib:database:fe"))
        }
        commonTest.dependencies {
            implementation(project(":feat:projects:fe:driven:test"))
            implementation(project(":feat:inspections:fe:driven:test"))
        }
    }
}
