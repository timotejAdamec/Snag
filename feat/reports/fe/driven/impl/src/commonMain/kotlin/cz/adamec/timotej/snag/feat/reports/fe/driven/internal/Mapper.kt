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

import cz.adamec.timotej.snag.reports.business.ReportType

internal fun ReportType.toQueryParam(): String =
    when (this) {
        ReportType.PASSPORT -> "passport"
        ReportType.SERVICE_PROTOCOL -> "service_protocol"
    }
