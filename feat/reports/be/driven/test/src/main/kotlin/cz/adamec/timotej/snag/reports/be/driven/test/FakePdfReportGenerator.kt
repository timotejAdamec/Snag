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

package cz.adamec.timotej.snag.reports.be.driven.test

import cz.adamec.timotej.snag.reports.be.ports.PdfReportGenerator
import cz.adamec.timotej.snag.reports.be.ports.ProjectReportData
import cz.adamec.timotej.snag.reports.business.ReportType

class FakePdfReportGenerator : PdfReportGenerator {
    var lastData: ProjectReportData? = null
    var lastType: ReportType? = null

    override suspend fun generate(
        data: ProjectReportData,
        type: ReportType,
    ): ByteArray {
        lastData = data
        lastType = type
        return FAKE_PDF_BYTES
    }

    companion object {
        val FAKE_PDF_BYTES = byteArrayOf(0x25, 0x50, 0x44, 0x46)
    }
}
