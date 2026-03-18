package cz.adamec.timotej.snag.reports.be.model

import cz.adamec.timotej.snag.reports.business.Report
import kotlin.uuid.Uuid

interface BackendReport : Report

data class BackendReportData(
    override val projectId: Uuid,
    override val fileName: String,
    override val bytes: ByteArray,
) : BackendReport {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BackendReportData) return false
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

    override fun toString(): String = "BackendReportData(projectId=$projectId, fileName=$fileName, bytes=${bytes.size} bytes)"
}
