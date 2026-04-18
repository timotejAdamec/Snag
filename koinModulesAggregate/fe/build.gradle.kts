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
            implementation(projects.lib.design.fe)
            implementation(projects.core.foundation.fe)
            implementation(projects.lib.network.fe.impl)
            implementation(projects.lib.storage.fe.impl)
            implementation(projects.feat.shared.database.fe.impl)
            implementation(projects.feat.shared.storage.fe)
            implementation(projects.core.business.rules.impl)

            // Authentication
            implementation(projects.feat.authentication.fe.app.impl)
            implementation(projects.feat.authentication.fe.driven.impl)
            implementation(projects.feat.authentication.fe.driving.impl)

            // Sync layer
            implementation(projects.feat.sync.fe.driven.impl)
            implementation(projects.feat.sync.fe.app.impl)

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

            // Clients feature
            implementation(projects.feat.clients.fe.app.impl)
            implementation(projects.feat.clients.fe.driving.api)
            implementation(projects.feat.clients.fe.driving.impl)
            implementation(projects.feat.clients.fe.driven.impl)

            // Findings feature
            implementation(projects.feat.findings.fe.app.impl)
            implementation(projects.feat.findings.fe.driving.api)
            implementation(projects.feat.findings.fe.driving.impl)
            implementation(projects.feat.findings.fe.driven.impl)

            // Inspections feature
            implementation(projects.feat.inspections.fe.app.impl)
            implementation(projects.feat.inspections.fe.driving.api)
            implementation(projects.feat.inspections.fe.driving.impl)
            implementation(projects.feat.inspections.fe.driven.impl)

            // Reports feature
            implementation(projects.feat.reports.fe.app.impl)
            implementation(projects.feat.reports.fe.driven.impl)
            implementation(projects.feat.reports.business.rules)

            // Users feature
            implementation(projects.feat.users.fe.app.impl)
            implementation(projects.feat.users.fe.driving.api)
            implementation(projects.feat.users.fe.driving.impl)
            implementation(projects.feat.users.fe.driven.impl)
        }
    }
}
