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
    // Core infrastructure
    implementation(projects.lib.core.be)
    implementation(projects.lib.configuration.be.impl)
    implementation(projects.feat.shared.database.be.impl)
    implementation(projects.feat.shared.rules.business.impl)

    // Projects feature
    implementation(projects.feat.projects.be.driving.impl)
    implementation(projects.feat.projects.be.driven.impl)
    implementation(projects.feat.projects.be.app.impl)

    // Clients feature
    implementation(projects.feat.clients.be.driving.impl)
    implementation(projects.feat.clients.be.driven.impl)
    implementation(projects.feat.clients.be.app.impl)

    // Structures feature
    implementation(projects.feat.structures.be.driving.impl)
    implementation(projects.feat.structures.be.driven.impl)
    implementation(projects.feat.structures.be.app.impl)

    // Findings feature
    implementation(projects.feat.findings.be.driving.impl)
    implementation(projects.feat.findings.be.driven.impl)
    implementation(projects.feat.findings.be.app.impl)
}
