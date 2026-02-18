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

package cz.adamec.timotej.snag.feat.reports.fe.app.impl.internal

import cz.adamec.timotej.snag.feat.reports.fe.app.api.DownloadReportUseCase
import cz.adamec.timotej.snag.feat.reports.fe.model.FrontendReport
import cz.adamec.timotej.snag.feat.reports.fe.ports.ReportsApi
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.lib.core.fe.log
import kotlin.uuid.Uuid

internal class DownloadReportUseCaseImpl(
    private val reportsApi: ReportsApi,
) : DownloadReportUseCase {
    override suspend fun invoke(projectId: Uuid): OnlineDataResult<FrontendReport> {
        val result = reportsApi.downloadReport(projectId)
        LH.logger.log(result, "downloadReport for projectId=$projectId")
        return result
    }
}
