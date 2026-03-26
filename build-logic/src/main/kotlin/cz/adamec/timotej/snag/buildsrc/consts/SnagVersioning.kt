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
 * - **Semantic version** (`0.2.0`) — derived from the latest git tag (`v0.2.0`).
 *   Falls back to `"0.0.0"` when no tags exist (local dev without a release).
 * - **Version code** (`260326042`) — `YYMMDD * 1000 + (commitCount % 1000)`.
 *   Encodes the build date and approximate commit position.
 * - **Version name** (`0.2.0.260326042`) — composite of semantic version and version code.
 *
 * All functions return [Provider] values for Gradle configuration-cache compatibility.
 */
object SnagVersioning {

    fun semanticVersion(project: Project): Provider<String> =
        project.providers.exec {
            commandLine("git", "describe", "--tags", "--abbrev=0")
            isIgnoreExitValue = true
        }.standardOutput.asText.map { output ->
            val tag = output.trim()
            if (tag.startsWith("v")) tag.removePrefix("v") else if (tag.isNotEmpty()) tag else "0.0.0"
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
}
