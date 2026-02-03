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
            // Core infrastructure
            implementation(projects.lib.core.fe)
            implementation(projects.lib.network.fe)
            implementation(projects.feat.shared.database.fe)

            // Sync layer
            implementation(projects.lib.sync.fe.driven.impl)
            implementation(projects.lib.sync.fe.app.impl)

            // Projects feature
            implementation(projects.feat.projects.fe.app.impl)
            implementation(projects.feat.projects.fe.driving.api)
            implementation(projects.feat.projects.fe.driving.impl)
            implementation(projects.feat.projects.fe.driven.impl)

            // Structures feature
            implementation(projects.feat.structures.fe.app.impl)
            implementation(projects.feat.structures.fe.driving.api)
            implementation(projects.feat.structures.fe.driving.impl)
            implementation(projects.feat.structures.fe.driven.impl)

            // Findings feature
            implementation(projects.feat.findings.fe.app.impl)
            implementation(projects.feat.findings.fe.driving.api)
            implementation(projects.feat.findings.fe.driving.impl)
            implementation(projects.feat.findings.fe.driven.impl)
        }
    }
}
