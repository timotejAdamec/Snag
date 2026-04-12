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
import java.io.File

internal fun Project.configureSharingReport() {
    require(this == rootProject) {
        "SharingReportPlugin must be applied to the root project, was applied to $path"
    }

    tasks.register("sharingReport", SharingReportTask::class.java) {
        group = "reporting"
        description = "Emits per-(module × source set) metadata CSV for thesis evaluation"
        outputCsv.set(
            layout.buildDirectory.file("reports/sharing/sharing_report.csv"),
        )
        // Lazy sweep: the provider is evaluated only when this task's inputs are
        // materialized (task execution or config-cache serialization for an invocation
        // that actually requested the task). `./gradlew check` without `sharingReport`
        // in the requested graph pays zero cost for the subproject walk.
        subprojectInputs.set(
            project.providers.provider {
                val probedPluginIds = CANONICAL_SNAG_PLUGIN_IDS + PLATFORM_MARKER_PLUGIN_IDS
                val rootDir = rootProject.projectDir
                rootProject.subprojects.map { subproject ->
                    SharingReportSubprojectInput(
                        modulePath = subproject.path,
                        appliedPluginIds = probedPluginIds.filter { pluginId ->
                            subproject.pluginManager.hasPlugin(pluginId)
                        },
                        sourceSetDirs = discoverSourceSetDirs(subproject.projectDir, rootDir),
                    )
                }
            },
        )
    }
}

private fun discoverSourceSetDirs(projectDir: File, rootDir: File): List<SourceSetDir> {
    val srcDir = projectDir.resolve("src")
    if (!srcDir.isDirectory) return emptyList()

    // Point at the source-set root, not `kotlin/`, so SQLDelight-only modules still appear
    // in the report (with zero Kotlin LOC after the tokei join).
    return srcDir.listFiles { file -> file.isDirectory }
        ?.map { sourceSetDir ->
            SourceSetDir(
                name = sourceSetDir.name,
                absolutePath = sourceSetDir.absolutePath,
                relativePath = sourceSetDir.relativeTo(rootDir).invariantSeparatorsPath,
            )
        }
        ?.sortedBy { it.name }
        .orEmpty()
}
