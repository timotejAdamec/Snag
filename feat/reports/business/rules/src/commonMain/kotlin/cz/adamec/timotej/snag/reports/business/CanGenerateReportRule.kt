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

import cz.adamec.timotej.snag.authorization.business.UserRole
import cz.adamec.timotej.snag.users.business.User

class CanGenerateReportRule {
    operator fun invoke(
        user: User,
        type: ReportType,
    ): Boolean =
        when (user.role) {
            UserRole.ADMINISTRATOR -> true
            UserRole.PASSPORT_LEAD,
            UserRole.PASSPORT_TECHNICIAN,
            -> type == ReportType.PASSPORT
            UserRole.SERVICE_LEAD,
            UserRole.SERVICE_WORKER,
            -> type == ReportType.SERVICE_PROTOCOL
            null -> false
        }
}
