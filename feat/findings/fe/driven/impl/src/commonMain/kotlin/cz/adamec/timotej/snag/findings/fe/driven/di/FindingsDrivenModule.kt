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

package cz.adamec.timotej.snag.findings.fe.driven.di

import cz.adamec.timotej.snag.findings.fe.driven.internal.api.RealFindingsApi
import cz.adamec.timotej.snag.findings.fe.driven.internal.db.RealFindingsDb
import cz.adamec.timotej.snag.findings.fe.driven.internal.sync.FindingSyncHandler
import cz.adamec.timotej.snag.findings.fe.driven.internal.sync.RealFindingsSync
import cz.adamec.timotej.snag.findings.fe.ports.FindingsApi
import cz.adamec.timotej.snag.findings.fe.ports.FindingsDb
import cz.adamec.timotej.snag.findings.fe.ports.FindingsSync
import cz.adamec.timotej.snag.lib.core.common.di.getIoDispatcher
import cz.adamec.timotej.snag.lib.sync.fe.app.handler.SyncOperationHandler
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val findingsDrivenModule =
    module {
        factory {
            RealFindingsDb(
                findingEntityQueries = get(),
                ioDispatcher = getIoDispatcher(),
            )
        } bind FindingsDb::class
        factoryOf(::RealFindingsApi) bind FindingsApi::class
        factoryOf(::FindingSyncHandler) bind SyncOperationHandler::class
        factoryOf(::RealFindingsSync) bind FindingsSync::class
    }
