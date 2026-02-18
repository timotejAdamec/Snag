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

data class Report(
    val projectId: Uuid,
    val bytes: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Report) return false
        return projectId == other.projectId && bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int {
        var result = projectId.hashCode()
        result = 31 * result + bytes.contentHashCode()
        return result
    }
}
