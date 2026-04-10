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

package cz.adamec.timotej.snag.buildsrc.plugins

import cz.adamec.timotej.snag.buildsrc.configuration.analysis.CANONICAL_SNAG_PLUGIN_IDS
import cz.adamec.timotej.snag.buildsrc.configuration.analysis.SharingReportSubprojectInput
import cz.adamec.timotej.snag.buildsrc.configuration.analysis.SharingReportTask
import cz.adamec.timotej.snag.buildsrc.configuration.analysis.SourceSetDir
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

@Suppress("unused")
internal class SharingReportPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        require(target == target.rootProject) {
            "SharingReportPlugin must be applied to the root project, was applied to ${target.path}"
        }

        val task = target.tasks.register("sharingReport", SharingReportTask::class.java) {
            group = "reporting"
            description = "Emits per-(module × source set) metadata CSV for thesis evaluation"
            outputCsv.set(
                target.layout.buildDirectory.file("reports/sharing/sharing_report.csv"),
            )
        }

        target.gradle.projectsEvaluated {
            val inputs = target.rootProject.subprojects.map { subproject ->
                SharingReportSubprojectInput(
                    modulePath = subproject.path,
                    appliedSnagPluginIds = CANONICAL_SNAG_PLUGIN_IDS.filter { pluginId ->
                        subproject.pluginManager.hasPlugin(pluginId)
                    },
                    sourceSetDirs = discoverSourceSetDirs(subproject.projectDir),
                )
            }
            task.configure { subprojectInputs.set(inputs) }
        }
    }
}

private fun discoverSourceSetDirs(projectDir: File): List<SourceSetDir> {
    val srcDir = projectDir.resolve("src")
    if (!srcDir.isDirectory) return emptyList()

    // Emit one entry per existing source-set directory, pointing at the source-set root rather
    // than the `kotlin/` subdirectory. This keeps modules that contribute zero hand-written
    // Kotlin (for example SQLDelight-only modules whose source lives in `src/commonMain/sqldelight`)
    // visible in the report — they show up with zero Kotlin LOC after the tokei join, which is
    // the correct answer.
    return srcDir.listFiles { file -> file.isDirectory }
        ?.map { sourceSetDir ->
            SourceSetDir(
                name = sourceSetDir.name,
                absolutePath = sourceSetDir.absolutePath,
            )
        }
        ?.sortedBy { it.name }
        .orEmpty()
}
