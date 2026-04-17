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

import cz.adamec.timotej.snag.buildsrc.configuration.analysis.CsvWriter.csvEscape
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

internal data class SharingReportSubprojectInput(
    val modulePath: String,
    val appliedPluginIds: List<String>,
    val sourceSetDirs: List<SourceSetDir>,
) : java.io.Serializable {
    companion object {
        private const val serialVersionUID: Long = 2L
    }
}

internal abstract class SharingReportTask : DefaultTask() {

    @get:org.gradle.api.tasks.Input
    abstract val subprojectInputs: ListProperty<SharingReportSubprojectInput>

    @get:OutputFile
    abstract val outputCsv: RegularFileProperty

    @TaskAction
    fun run() {
        val rows = subprojectInputs.get().flatMap { input ->
            SharingReportRowBuilder.buildRows(
                modulePath = input.modulePath,
                appliedPluginIds = input.appliedPluginIds,
                sourceSetDirs = input.sourceSetDirs,
            )
        }

        val output: File = outputCsv.get().asFile
        CsvWriter.writeRows(
            outputFile = output,
            header = CSV_HEADER,
            rows = rows.map { it.toCsv() },
        )

        logger.lifecycle("SharingReport: wrote ${rows.size} rows to ${output.absolutePath}")
    }
}

private const val CSV_HEADER =
    "module_path,category,feature,platform,hex_layer,encapsulation,plugin_applied,source_set,source_set_dir,source_set_dir_rel,platform_set"

private fun SharingReportRow.toCsv(): String = listOf(
    modulePath,
    category,
    feature,
    platform,
    hexLayer,
    encapsulation,
    pluginApplied,
    sourceSet,
    sourceSetDir,
    sourceSetDirRel,
    platformSet,
).joinToString(",") { it.csvEscape() }
