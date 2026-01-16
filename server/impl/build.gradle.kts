import io.ktor.plugin.OpenApiPreview

// import com.github.psxpaul.task.JavaExecFork

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
    application
}

group = "cz.adamec.timotej.snag"
version = "1.0.0"
application {
    mainClass.set("cz.adamec.timotej.snag.impl.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

// Currently not working, only with Kotlin 2.2.0. Wait for newer ktor.
ktor {
    @OptIn(OpenApiPreview::class)
    openApi {
        title = "Snag server API"
        version = "2.1"
        summary = "This is an overview of the Snag backend"
        target = project.layout.buildDirectory.file("open-api.json")
    }
}

dependencies {
    implementation(projects.server.api)
    implementation(projects.feat.projects.be.driving.impl)
    implementation(projects.feat.projects.be.driven)
    implementation(projects.feat.projects.be.app)
    implementation(projects.feat.structures.be.driving.impl)

    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.config.yaml)
    implementation(libs.logback)
    implementation(libs.koin.logger.slf4j)
}
