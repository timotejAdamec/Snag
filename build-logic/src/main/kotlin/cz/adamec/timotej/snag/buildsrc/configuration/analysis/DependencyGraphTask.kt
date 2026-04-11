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

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

internal data class DependencyEdgeInput(
    val sourceModule: String,
    val sourceConfiguration: String,
    val targetModule: String,
    val scope: String,
) : java.io.Serializable {
    companion object {
        private const val serialVersionUID: Long = 1L
    }
}

internal abstract class DependencyGraphTask : DefaultTask() {

    @get:org.gradle.api.tasks.Input
    abstract val edges: ListProperty<DependencyEdgeInput>

    @get:OutputFile
    abstract val outputCsv: RegularFileProperty

    @TaskAction
    fun run() {
        val rows = DependencyGraphFormatter.sort(edges.get())

        val output: File = outputCsv.get().asFile
        output.parentFile.mkdirs()
        if (output.exists()) output.delete()

        output.bufferedWriter().use { writer ->
            writer.append(DependencyGraphFormatter.CSV_HEADER)
            writer.append('\n')
            for (row in rows) {
                writer.append(DependencyGraphFormatter.toCsv(row))
                writer.append('\n')
            }
        }

        logger.lifecycle("DependencyGraph: wrote ${rows.size} edges to ${output.absolutePath}")
    }
}

internal object DependencyGraphFormatter {

    const val CSV_HEADER = "source_module,source_configuration,target_module,scope"

    fun sort(edges: List<DependencyEdgeInput>): List<DependencyEdgeInput> = edges
        .sortedWith(
            compareBy(
                { it.sourceModule },
                { it.sourceConfiguration },
                { it.targetModule },
            ),
        )

    fun toCsv(edge: DependencyEdgeInput): String = listOf(
        edge.sourceModule,
        edge.sourceConfiguration,
        edge.targetModule,
        edge.scope,
    ).joinToString(",") { it.csvEscape() }

    fun normalizePath(path: String): String = if (path.startsWith(":")) path else ":$path"

    fun scopeOf(configurationName: String): String = when {
        configurationName == "implementation" -> "implementation"
        configurationName == "api" -> "api"
        configurationName == "runtimeOnly" -> "runtimeOnly"
        configurationName.endsWith("Implementation") -> "implementation"
        configurationName.endsWith("Api") -> "api"
        configurationName.endsWith("RuntimeOnly") -> "runtimeOnly"
        else -> ""
    }

    fun shouldIncludeConfiguration(configurationName: String): Boolean {
        if (configurationName in PLAIN_CONFIG_NAMES) return true
        return CONFIG_NAME_SUFFIXES.any { suffix -> configurationName.endsWith(suffix) }
    }

    private val CONFIG_NAME_SUFFIXES = listOf("Implementation", "Api", "RuntimeOnly")
    private val PLAIN_CONFIG_NAMES = setOf("implementation", "api", "runtimeOnly")

    private fun String.csvEscape(): String {
        val needsQuoting = contains(',') || contains('"') || contains('\n') || contains('\r')
        if (!needsQuoting) return this
        val escaped = replace("\"", "\"\"")
        return "\"$escaped\""
    }
}
