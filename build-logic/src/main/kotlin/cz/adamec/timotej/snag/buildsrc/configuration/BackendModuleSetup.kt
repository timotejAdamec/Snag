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
import cz.adamec.timotej.snag.buildsrc.extensions.implementation
import cz.adamec.timotej.snag.buildsrc.extensions.library
import cz.adamec.timotej.snag.buildsrc.extensions.libs
import cz.adamec.timotej.snag.buildsrc.extensions.testImplementation
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmExtension
import java.io.File
import java.util.Properties

internal fun Project.configureBackendModule() {
    dependencies {
        if (!path.startsWith(":core")) {
            implementation(project(":core:foundation:be"))
        }
        if (path.startsWith(":feat") && !path.startsWith(":feat:sync") && path.contains(":be:driven:impl")) {
            implementation(project(":feat:sync:be:api"))
        }
        if (path.startsWith(":feat") && !path.startsWith(":feat:sync") && path.contains(":be:app:model")) {
            api(project(":feat:sync:be:model"))
        }

        for (dep in resolveHexagonalDependencies()) {
            when (dep.scope) {
                DependencyScope.API -> api(project(dep.projectPath))
                DependencyScope.IMPLEMENTATION -> implementation(project(dep.projectPath))
            }
        }

        implementation(libs.library("kotlinx-coroutines-core"))
        implementation(libs.library("koin-core"))
        implementation(libs.library("slf4j-api"))

        if (!path.contains("testInfra")) {
            testImplementation(project(":testInfra:be"))
        }
        testImplementation(libs.library("kotlin-test-junit"))
        testImplementation(libs.library("kotlinx-coroutines-test"))
    }

    tasks.withType<Test> {
        loadEnvFile(rootProject.file("config/backend-local.env"))
        loadPropertiesAsEnv(rootProject.file("config/common-debug.properties"))
    }

    extensions.findByType(KotlinJvmExtension::class.java)?.apply {
        this.compilerOptions {
            freeCompilerArgs.add("-opt-in=kotlin.uuid.ExperimentalUuidApi")
        }
    }
}

private fun Test.loadEnvFile(file: File) {
    if (!file.exists()) return
    for (line in file.readLines()) {
        if (line.isNotBlank() && !line.startsWith("#")) {
            val (key, value) = line.split("=", limit = 2)
            environment(key.trim(), value.trim())
        }
    }
}

/**
 * Loads a `.properties` file and sets each property as an environment variable
 * with key converted from `snag.camelCase` to `SNAG_UPPER_SNAKE_CASE`.
 */
private fun Test.loadPropertiesAsEnv(file: File) {
    if (!file.exists()) return
    val props = Properties().apply { file.inputStream().use { load(it) } }
    for ((key, value) in props) {
        val envKey =
            (key as String)
                .replace(".", "_")
                .replace(Regex("([a-z])([A-Z])")) { "${it.groupValues[1]}_${it.groupValues[2]}" }
                .uppercase()
        environment(envKey, value as String)
    }
}
