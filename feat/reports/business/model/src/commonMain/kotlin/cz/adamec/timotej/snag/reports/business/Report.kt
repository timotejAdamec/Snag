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

package cz.adamec.timotej.snag.reports.business

import kotlin.uuid.Uuid

interface Report {
    val projectId: Uuid

    val fileName: String

    val bytes: ByteArray
}

data class ReportData(
    override val projectId: Uuid,
    override val fileName: String,
    override val bytes: ByteArray,
) : Report {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ReportData) return false
        return projectId == other.projectId &&
            fileName == other.fileName &&
            bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int {
        var result = projectId.hashCode()
        result = 31 * result + fileName.hashCode()
        result = 31 * result + bytes.contentHashCode()
        return result
    }

    override fun toString(): String = "ReportData(projectId=$projectId, fileName=$fileName, bytes=${bytes.size} bytes)"
}
