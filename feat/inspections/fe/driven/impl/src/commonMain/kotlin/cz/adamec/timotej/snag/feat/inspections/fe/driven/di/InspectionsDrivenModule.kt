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

package cz.adamec.timotej.snag.feat.inspections.fe.driven.di

import cz.adamec.timotej.snag.feat.inspections.fe.driven.internal.api.RealInspectionsApi
import cz.adamec.timotej.snag.feat.inspections.fe.driven.internal.db.InspectionsSqlDelightDbOps
import cz.adamec.timotej.snag.feat.inspections.fe.driven.internal.db.RealInspectionsDb
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsApi
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsDb
import cz.adamec.timotej.snag.lib.core.common.di.getIoDispatcher
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val inspectionsDrivenModule =
    module {
        factory {
            InspectionsSqlDelightDbOps(
                queries = get(),
                ioDispatcher = getIoDispatcher(),
            )
        }
        factory { RealInspectionsDb(ops = get()) } bind InspectionsDb::class
        factoryOf(::RealInspectionsApi) bind InspectionsApi::class
    }
