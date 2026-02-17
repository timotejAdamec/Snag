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

package cz.adamec.timotej.snag.reports.be.driven.impl.di

import cz.adamec.timotej.snag.reports.be.driven.impl.internal.OpenPdfReportGenerator
import cz.adamec.timotej.snag.reports.be.ports.PdfReportGenerator
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val reportsDrivenModule =
    module {
        singleOf(::OpenPdfReportGenerator) bind PdfReportGenerator::class
    }
