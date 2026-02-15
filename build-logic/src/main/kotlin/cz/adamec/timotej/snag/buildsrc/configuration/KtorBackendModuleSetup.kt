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

package cz.adamec.timotej.snag.buildsrc.configuration

import cz.adamec.timotej.snag.buildsrc.extensions.implementation
import cz.adamec.timotej.snag.buildsrc.extensions.library
import cz.adamec.timotej.snag.buildsrc.extensions.libs
import cz.adamec.timotej.snag.buildsrc.extensions.testImplementation
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureNetworkBackendModule() {
    dependencies {
        if (!path.contains("routing")) {
            implementation(project(":lib:routing:be"))
        }

        implementation(libs.library("ktor-server-core"))
        implementation(libs.library("koin-ktor"))
        implementation(libs.library("ktor-serialization-kotlinx-json"))
        testImplementation(project(":lib:network:be:test"))
    }
}
