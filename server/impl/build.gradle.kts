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
    alias(libs.plugins.snagImplDrivingBackendModule)
    alias(libs.plugins.ktor)
}

group = "cz.adamec.timotej.snag"
version = "1.0.0"
application {
    mainClass.set("cz.adamec.timotej.snag.impl.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

ktor {
    openApi {
        enabled = true
        codeInferenceEnabled = true
        onlyCommented = false
    }
}

// Ktor plugin + application plugin duplicate the module JAR in distributions
tasks.withType<Tar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
tasks.withType<Zip> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

dependencies {
    implementation(projects.server.api)
    implementation(projects.koinModulesAggregate.be)
    implementation(projects.lib.configuration.be.api)
    implementation(projects.lib.storage.be.api)
    implementation(projects.lib.storage.be.impl)
    implementation(projects.feat.projects.be.ports)
    implementation(projects.feat.clients.be.ports)
    implementation(projects.feat.structures.be.ports)
    implementation(projects.feat.findings.be.ports)
    implementation(projects.feat.inspections.be.ports)

    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.config.yaml)
    implementation(libs.logback)
    implementation(libs.koin.logger.slf4j)
}
