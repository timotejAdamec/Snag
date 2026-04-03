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

package cz.adamec.timotej.snag.buildsrc.consts

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Centralized version computation for all platforms.
 *
 * Frontend and backend use independent version tags with prefixes [FE_TAG_PREFIX] (`fe-v`)
 * and [BE_TAG_PREFIX] (`be-v`). The active prefix is auto-detected from the Gradle task graph:
 * if `buildFatJar` is among the requested tasks, the backend prefix is used; otherwise frontend.
 *
 * - **Semantic version** (`0.2.0`) — derived from the nearest matching git tag (`fe-v0.2.0`
 *   or `be-v0.2.0`). Falls back to `"0.0.0"` when no matching tags exist.
 * - **Version code** (`260326042`) — `YYMMDD * 1000 + (commitCount % 1000)`.
 *   Encodes the build date and approximate commit position.
 * - **Version name** (`0.2.0.260326042`) — composite of semantic version and version code.
 *
 * All functions return [Provider] values for Gradle configuration-cache compatibility.
 */
object SnagVersioning {

    private const val FE_TAG_PREFIX = "fe-v"
    private const val BE_TAG_PREFIX = "be-v"

    fun semanticVersion(project: Project): Provider<String> {
        val tagPrefix = resolveTagPrefix(project)
        return project.providers.exec {
            commandLine(
                "git", "describe",
                "--tags", "--abbrev=0",
                "--match", "${tagPrefix}*",
            )
            isIgnoreExitValue = true
        }.standardOutput.asText.map { output ->
            val tag = output.trim()
            val version = tag.removePrefix(tagPrefix)
            version.ifEmpty { "0.0.0" }
        }
    }

    fun versionCode(project: Project): Provider<Int> {
        val commitCount = project.providers.exec {
            commandLine("git", "rev-list", "--count", "HEAD")
        }.standardOutput.asText.map { it.trim().toInt() }

        return commitCount.map { count ->
            val datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd")).toInt()
            datePart * 1000 + (count % 1000)
        }
    }

    fun versionName(project: Project): Provider<String> {
        val semantic = semanticVersion(project)
        val code = versionCode(project)
        return semantic.zip(code) { s, c -> "$s.$c" }
    }

    private fun resolveTagPrefix(project: Project): String {
        val isBackend = project.gradle.startParameter.taskNames.any {
            it.contains("buildFatJar", ignoreCase = true)
        }
        return if (isBackend) BE_TAG_PREFIX else FE_TAG_PREFIX
    }
}
