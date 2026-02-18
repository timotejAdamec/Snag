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

package cz.adamec.timotej.snag.feat.reports.fe.driven.internal

import cz.adamec.timotej.snag.feat.reports.fe.model.FrontendReport
import cz.adamec.timotej.snag.feat.reports.fe.ports.ReportsApi
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.network.fe.SnagNetworkHttpClient
import cz.adamec.timotej.snag.network.fe.safeApiCall
import cz.adamec.timotej.snag.reports.business.Report
import io.ktor.client.call.body
import kotlin.uuid.Uuid

internal class RealReportsApi(
    private val httpClient: SnagNetworkHttpClient,
) : ReportsApi {
    override suspend fun downloadReport(projectId: Uuid): OnlineDataResult<FrontendReport> =
        safeApiCall(
            logger = LH.logger,
            errorContext = "Error downloading report for project $projectId.",
        ) {
            val bytes = httpClient.get("/projects/$projectId/report").body<ByteArray>()
            FrontendReport(
                report =
                    Report(
                        projectId = projectId,
                        bytes = bytes,
                    ),
            )
        }
}
