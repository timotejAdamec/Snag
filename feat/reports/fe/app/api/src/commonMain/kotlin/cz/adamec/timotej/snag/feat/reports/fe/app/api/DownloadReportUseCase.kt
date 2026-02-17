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

package cz.adamec.timotej.snag.feat.reports.fe.app.api

import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import kotlin.uuid.Uuid

interface DownloadReportUseCase {
    suspend operator fun invoke(projectId: Uuid): OnlineDataResult<ByteArray>
}
