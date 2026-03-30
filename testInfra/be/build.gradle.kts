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
    alias(libs.plugins.snagBackendModule)
}

dependencies {
    api(projects.testInfra.common)
    api(projects.feat.users.be.driven.test)
    api(projects.feat.projects.be.driven.test)
    api(projects.feat.structures.be.driven.test)
    api(projects.feat.findings.be.driven.test)
    api(projects.feat.inspections.be.driven.test)
    api(projects.feat.clients.be.driven.test)
    implementation(projects.koinModulesAggregate.be)
    implementation(projects.feat.authentication.be.driving.test)
    implementation(projects.feat.shared.database.be.test)
    implementation(projects.lib.storage.be.test)
    implementation(projects.feat.reports.be.driven.test)
}
