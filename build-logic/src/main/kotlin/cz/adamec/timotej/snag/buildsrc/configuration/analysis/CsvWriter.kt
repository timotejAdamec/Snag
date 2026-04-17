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

import java.io.File

internal object CsvWriter {

    fun writeRows(
        outputFile: File,
        header: String,
        rows: Iterable<String>,
    ) {
        outputFile.parentFile.mkdirs()
        if (outputFile.exists()) outputFile.delete()
        outputFile.bufferedWriter().use { writer ->
            writer.append(header)
            writer.append('\n')
            for (row in rows) {
                writer.append(row)
                writer.append('\n')
            }
        }
    }

    fun String.csvEscape(): String {
        val needsQuoting = contains(',') || contains('"') || contains('\n') || contains('\r')
        if (!needsQuoting) return this
        val escaped = replace("\"", "\"\"")
        return "\"$escaped\""
    }
}
