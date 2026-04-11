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

package cz.adamec.timotej.snag.buildsrc.configuration.analysis

import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency

// Source-set-aware: the sweep picks up `commonMainImplementation`, `androidMainImplementation`,
// `mobileMainApi`, plain JVM `implementation`, etc. Resolution is not triggered — we read
// `configuration.dependencies` directly, which exposes declared dependencies without forcing
// graph resolution. Phase 2 ripple analysis needs per-source-set edges so `feature_retro.py`
// can compute blast radius at the `(module, source_set)` level, not just module-to-module.
internal fun Project.configureDependencyGraph() {
    require(this == rootProject) {
        "DependencyGraph setup must be applied to the root project, was applied to $path"
    }

    val task = tasks.register("dependencyGraphReport", DependencyGraphTask::class.java) {
        group = "reporting"
        description = "Emits per-(module × configuration) project dependency edges for thesis evaluation"
        outputCsv.set(
            layout.buildDirectory.file("reports/dependency_graph/dependency_graph.csv"),
        )
    }

    gradle.projectsEvaluated {
        val collected = rootProject.subprojects.flatMap { subproject ->
            collectProjectEdges(subproject)
        }
        task.configure { edges.set(collected) }
    }
}

private fun collectProjectEdges(subproject: Project): List<DependencyEdgeInput> {
    val sourceModule = subproject.path
    return subproject.configurations
        .asSequence()
        .filter { configuration -> DependencyGraphFormatter.shouldIncludeConfiguration(configuration.name) }
        .flatMap { configuration ->
            configuration.dependencies
                .asSequence()
                .filterIsInstance<ProjectDependency>()
                .map { dep ->
                    DependencyEdgeInput(
                        sourceModule = sourceModule,
                        sourceConfiguration = configuration.name,
                        targetModule = DependencyGraphFormatter.normalizePath(dep.path),
                        scope = DependencyGraphFormatter.scopeOf(configuration.name),
                    )
                }
        }
        .toList()
}
