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

package cz.adamec.timotej.snag.reports.be.app.api

import cz.adamec.timotej.snag.reports.business.ReportType
import kotlin.uuid.Uuid

interface CanGenerateReportUseCase {
    suspend operator fun invoke(
        userId: Uuid,
        type: ReportType,
    ): Boolean
}
