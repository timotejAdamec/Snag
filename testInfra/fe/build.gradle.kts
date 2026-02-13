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
    alias(libs.plugins.snagFrontendMultiplatformModule)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.testInfra.common)
            implementation(projects.koinModulesAggregate.fe)
            implementation(projects.feat.shared.database.fe.test)
            implementation(projects.feat.inspections.fe.driven.test)
        }
    }
}
