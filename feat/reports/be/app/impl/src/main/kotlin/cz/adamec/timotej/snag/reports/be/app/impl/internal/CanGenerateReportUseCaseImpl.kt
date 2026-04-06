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

package cz.adamec.timotej.snag.reports.be.app.impl.internal

import cz.adamec.timotej.snag.reports.be.app.api.CanGenerateReportUseCase
import cz.adamec.timotej.snag.reports.business.CanGenerateReportRule
import cz.adamec.timotej.snag.reports.business.ReportType
import cz.adamec.timotej.snag.users.be.app.api.GetUserUseCase
import kotlin.uuid.Uuid

internal class CanGenerateReportUseCaseImpl(
    private val getUserUseCase: GetUserUseCase,
    private val canGenerateReportRule: CanGenerateReportRule,
) : CanGenerateReportUseCase {
    override suspend operator fun invoke(
        userId: Uuid,
        type: ReportType,
    ): Boolean {
        val user = getUserUseCase(userId) ?: return false
        return canGenerateReportRule(user, type)
    }
}
