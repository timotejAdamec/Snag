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
            implementation(projects.featShared.database.fe.driven.test)
            implementation(projects.feat.authentication.fe.driven.test)
            implementation(projects.feat.clients.fe.driven.test)
            implementation(projects.feat.findings.fe.driven.test)
            implementation(projects.feat.inspections.fe.driven.test)
            implementation(projects.feat.projects.fe.driven.test)
            implementation(projects.feat.reports.fe.driven.test)
            implementation(projects.feat.structures.fe.driven.test)
            implementation(projects.feat.users.fe.driven.test)
            implementation(projects.lib.network.fe.test)
            implementation(projects.lib.storage.fe.test)
            implementation(projects.feat.sync.fe.driven.test)
        }
    }
}
