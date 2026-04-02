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

package cz.adamec.timotej.snag.buildsrc.configuration.architecture

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

internal abstract class ArchitectureCheckTask : DefaultTask() {

    @get:Input
    abstract val modulePath: Property<String>

    @get:Input
    abstract val projectDependencies: SetProperty<String>

    @TaskAction
    fun check() {
        val source = parseModulePath(modulePath.get())
        val violations = projectDependencies.get()
            .map { parseModulePath(it) }
            .flatMap { target -> checkDependency(source, target) }
            .filter { !isAllowlisted(it.source, it.target) }

        if (violations.isNotEmpty()) {
            val message = buildString {
                appendLine()
                appendLine("Architecture violations in ${modulePath.get()}:")
                appendLine()
                for (violation in violations) {
                    appendLine("  [${violation.ruleId}] ${violation.source} -> ${violation.target}")
                    appendLine("    ${violation.message}")
                    appendLine()
                }
                appendLine("FAILED: ${violations.size} architecture violation(s) found.")
            }
            throw GradleException(message)
        }
    }
}

private val PRODUCTION_KMP_CONFIGS = listOf(
    "commonMainApi",
    "commonMainImplementation",
)

private val PRODUCTION_JVM_CONFIGS = listOf(
    "api",
    "implementation",
)

private fun Project.collectProjectDependencies(): Set<String> {
    val isKmp = configurations.findByName("commonMainImplementation") != null
    val configNames = if (isKmp) PRODUCTION_KMP_CONFIGS else PRODUCTION_JVM_CONFIGS

    return configNames
        .mapNotNull { configurations.findByName(it) }
        .flatMap { config ->
            config.dependencies
                .filterIsInstance<ProjectDependency>()
                .map { dep ->
                    val depPath = dep.path
                    if (depPath.startsWith(":")) depPath else ":$depPath"
                }
        }
        .toSet()
}

fun Project.configureArchitectureCheck() {
    afterEvaluate {
        val deps = collectProjectDependencies()
        val projectPath = path

        tasks.register("archCheck", ArchitectureCheckTask::class.java) {
            group = "verification"
            description = "Checks module dependencies against architectural rules"
            modulePath.set(projectPath)
            projectDependencies.set(deps)
        }

        tasks.named("check").configure {
            dependsOn("archCheck")
        }
    }
}
