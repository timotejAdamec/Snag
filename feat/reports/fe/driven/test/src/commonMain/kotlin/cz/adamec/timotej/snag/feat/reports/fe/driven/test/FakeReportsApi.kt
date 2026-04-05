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

package cz.adamec.timotej.snag.feat.reports.fe.driven.test

import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.feat.reports.fe.ports.ReportsApi
import cz.adamec.timotej.snag.reports.business.Report
import cz.adamec.timotej.snag.reports.business.ReportData
import cz.adamec.timotej.snag.reports.business.ReportType
import kotlinx.coroutines.CompletableDeferred
import kotlin.uuid.Uuid

class FakeReportsApi : ReportsApi {
    var forcedFailure: OnlineDataResult.Failure? = null
    var reportBytes: ByteArray = byteArrayOf()
    var reportFileName: String = "report.pdf"
    var downloadDeferred: CompletableDeferred<Unit>? = null
    val downloadedProjectIds = mutableListOf<Uuid>()
    val downloadedReportTypes = mutableListOf<ReportType>()

    override suspend fun downloadReport(
        projectId: Uuid,
        type: ReportType,
    ): OnlineDataResult<Report> {
        downloadedProjectIds.add(projectId)
        downloadedReportTypes.add(type)
        downloadDeferred?.await()
        val failure = forcedFailure
        if (failure != null) return failure
        return OnlineDataResult.Success(
            ReportData(
                projectId = projectId,
                fileName = reportFileName,
                bytes = reportBytes,
            ),
        )
    }
}
