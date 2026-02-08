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

package cz.adamec.timotej.snag.clients.fe.driven.di

import cz.adamec.timotej.snag.clients.fe.driven.internal.api.RealClientsApi
import cz.adamec.timotej.snag.clients.fe.driven.internal.db.RealClientsDb
import cz.adamec.timotej.snag.clients.fe.driven.internal.sync.ClientSyncHandler
import cz.adamec.timotej.snag.clients.fe.driven.internal.sync.RealClientsPullSyncCoordinator
import cz.adamec.timotej.snag.clients.fe.driven.internal.sync.RealClientsPullSyncTimestampDataSource
import cz.adamec.timotej.snag.clients.fe.driven.internal.sync.RealClientsSync
import cz.adamec.timotej.snag.clients.fe.ports.ClientsApi
import cz.adamec.timotej.snag.clients.fe.ports.ClientsDb
import cz.adamec.timotej.snag.clients.fe.ports.ClientsPullSyncCoordinator
import cz.adamec.timotej.snag.clients.fe.ports.ClientsPullSyncTimestampDataSource
import cz.adamec.timotej.snag.clients.fe.ports.ClientsSync
import cz.adamec.timotej.snag.lib.core.common.di.getIoDispatcher
import cz.adamec.timotej.snag.lib.sync.fe.app.api.handler.SyncOperationHandler
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val clientsDrivenModule =
    module {
        factory {
            RealClientsDb(
                clientEntityQueries = get(),
                ioDispatcher = getIoDispatcher(),
            )
        } bind ClientsDb::class
        factoryOf(::RealClientsApi) bind ClientsApi::class
        factoryOf(::ClientSyncHandler) bind SyncOperationHandler::class
        factoryOf(::RealClientsSync) bind ClientsSync::class
        factory {
            RealClientsPullSyncTimestampDataSource(
                queries = get(),
                ioDispatcher = getIoDispatcher(),
            )
        } bind ClientsPullSyncTimestampDataSource::class
        factoryOf(::RealClientsPullSyncCoordinator) bind ClientsPullSyncCoordinator::class
    }
