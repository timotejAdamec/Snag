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

import cz.adamec.timotej.snag.buildsrc.extensions.library
import org.gradle.api.Project
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun Project.configureNetworkFrontendMultiplatformModule() {
    extensions.findByType(KotlinMultiplatformExtension::class.java)?.apply {
        sourceSets {
            commonMain.dependencies {
                if (!path.contains("network")) {
                    implementation(project(":lib:network:fe"))
                }
                if (path.endsWith(":driven:test")) {
                    implementation(project(":lib:network:fe:test"))
                }
                implementation(library("kotlinx-serialization-core"))
                implementation(library("ktor-client-core"))
                implementation(library("ktor-client-content-negotiation"))
                implementation(library("ktor-client-logging"))
                implementation(library("ktor-serialization-kotlinx-json"))
            }
            androidMain.dependencies {
                implementation(library("ktor-client-okhttp"))
            }
            jvmMain.dependencies {
                implementation(library("ktor-client-okhttp"))
            }
            iosMain.dependencies {
                implementation(library("ktor-client-darwin"))
            }
            wasmJsMain.dependencies {
                implementation(library("ktor-client-cio"))
            }
            jsMain.dependencies {
                implementation(library("ktor-client-js"))
            }
        }
    }
}
