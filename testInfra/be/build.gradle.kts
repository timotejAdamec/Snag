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
    implementation(projects.koinModulesAggregate.be)
    implementation(projects.feat.shared.database.be.test)
    implementation(projects.lib.storage.be.test)
    implementation(projects.feat.reports.be.driven.test)
}
