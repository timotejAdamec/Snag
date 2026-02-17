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

import cz.adamec.timotej.snag.feat.reports.fe.ports.ReportsApi
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import kotlin.uuid.Uuid

class FakeReportsApi : ReportsApi {
    var forcedFailure: OnlineDataResult.Failure? = null
    var reportBytes: ByteArray = byteArrayOf()
    val downloadedProjectIds = mutableListOf<Uuid>()

    override suspend fun downloadReport(projectId: Uuid): OnlineDataResult<ByteArray> {
        downloadedProjectIds.add(projectId)
        val failure = forcedFailure
        if (failure != null) return failure
        return OnlineDataResult.Success(reportBytes)
    }
}
