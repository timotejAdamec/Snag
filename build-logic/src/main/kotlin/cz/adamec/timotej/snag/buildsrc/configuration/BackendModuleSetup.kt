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
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmExtension

internal fun Project.configureBackendModule() {
    dependencies {
        implementation(project(":lib:core"))

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
