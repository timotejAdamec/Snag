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

import cz.adamec.timotej.snag.buildsrc.extensions.api
import cz.adamec.timotej.snag.buildsrc.extensions.hasFolderInPath
import cz.adamec.timotej.snag.buildsrc.extensions.implementation
import cz.adamec.timotej.snag.buildsrc.extensions.library
import cz.adamec.timotej.snag.buildsrc.extensions.libs
import cz.adamec.timotej.snag.buildsrc.extensions.testImplementation
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmExtension

internal fun Project.configureBackendModule() {
    dependencies {
        if (!path.contains("core")) {
            implementation(project(":lib:core:be"))
        }

        val moduleDirectoryPath = path.substringBeforeLast(":")
        val modulePreDirectoryPath = moduleDirectoryPath.substringBeforeLast(":")
        if (name == "model") {
            api(project("$modulePreDirectoryPath:business"))
        } else if (name == "ports") {
            if (hasFolderInPath(moduleDirectoryPath, "model")) {
                api(project("$moduleDirectoryPath:model"))
            } else {
                api(project("$modulePreDirectoryPath:business"))
            }
        } else if (name == "app") {
            implementation(project("$moduleDirectoryPath:ports"))
        } else if (path.contains(":app:") && name == "api") {
            val feOrBeDirectoryPath = moduleDirectoryPath.substringBeforeLast(":app")
            val businessDirectoryPath = feOrBeDirectoryPath.substringBeforeLast(":")
            if (hasFolderInPath(feOrBeDirectoryPath, "model")) {
                api(project("$feOrBeDirectoryPath:model"))
            } else {
                api(project("$businessDirectoryPath:business"))
            }
        } else if (path.contains(":app:") && name == "impl") {
            val feOrBeDirectoryPath = moduleDirectoryPath.substringBeforeLast(":app")
            implementation(project("$moduleDirectoryPath:api"))
            implementation(project("$feOrBeDirectoryPath:ports"))
        } else if (path.contains("driven")) {
            val drivenDirectoryPath = moduleDirectoryPath.substringBeforeLast(":driven")
            api(project("$drivenDirectoryPath:ports"))
        }

        implementation(libs.library("kotlinx-coroutines-core"))
        implementation(libs.library("koin-core"))
        implementation(libs.library("slf4j-api"))
        testImplementation(libs.library("kotlin-test-junit"))
        testImplementation(libs.library("kotlinx-coroutines-test"))
    }

    extensions.findByType(KotlinJvmExtension::class.java)?.apply {
        this.compilerOptions {
            freeCompilerArgs.add("-opt-in=kotlin.uuid.ExperimentalUuidApi")
        }
    }
}
