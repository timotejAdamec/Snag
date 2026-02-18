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

import cz.adamec.timotej.snag.feat.reports.fe.model.FrontendReport
import cz.adamec.timotej.snag.feat.reports.fe.ports.ReportsApi
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.reports.business.Report
import kotlin.uuid.Uuid

class FakeReportsApi : ReportsApi {
    var forcedFailure: OnlineDataResult.Failure? = null
    var reportBytes: ByteArray = byteArrayOf()
    var reportFileName: String = "report.pdf"
    val downloadedProjectIds = mutableListOf<Uuid>()

    override suspend fun downloadReport(projectId: Uuid): OnlineDataResult<FrontendReport> {
        downloadedProjectIds.add(projectId)
        val failure = forcedFailure
        if (failure != null) return failure
        return OnlineDataResult.Success(
            FrontendReport(
                report =
                    Report(
                        projectId = projectId,
                        fileName = reportFileName,
                        bytes = reportBytes,
                    ),
            ),
        )
    }
}
