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

package cz.adamec.timotej.snag.feat.reports.fe.driven.di

import cz.adamec.timotej.snag.feat.reports.fe.driven.internal.RealReportsApi
import cz.adamec.timotej.snag.feat.reports.fe.ports.ReportsApi
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val reportsDrivenModule =
    module {
        factoryOf(::RealReportsApi) bind ReportsApi::class
    }
