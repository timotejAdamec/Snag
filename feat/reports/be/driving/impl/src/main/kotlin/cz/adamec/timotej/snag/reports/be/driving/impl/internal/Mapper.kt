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

package cz.adamec.timotej.snag.reports.be.driving.impl.internal

import cz.adamec.timotej.snag.reports.business.ReportType
import cz.adamec.timotej.snag.reports.contract.ReportTypeParam

internal fun parseReportType(value: String?): ReportType? =
    when (value) {
        null, ReportTypeParam.PASSPORT -> ReportType.PASSPORT
        ReportTypeParam.SERVICE_PROTOCOL -> ReportType.SERVICE_PROTOCOL
        else -> null
    }
