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

package cz.adamec.timotej.snag.projects.fe.driven.di

import cz.adamec.timotej.snag.lib.core.common.di.getIoDispatcher
import cz.adamec.timotej.snag.lib.sync.fe.app.SyncEnqueuer
import cz.adamec.timotej.snag.lib.sync.fe.app.handler.SyncOperationHandler
import cz.adamec.timotej.snag.projects.fe.driven.internal.api.RealProjectsApi
import cz.adamec.timotej.snag.projects.fe.driven.internal.db.RealProjectsDb
import cz.adamec.timotej.snag.projects.fe.driven.internal.sync.PROJECT_SYNC_ENTITY_TYPE
import cz.adamec.timotej.snag.projects.fe.driven.internal.sync.ProjectSyncHandler
import cz.adamec.timotej.snag.projects.fe.driven.internal.sync.RealProjectsSync
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsApi
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsDb
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsSync
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val projectsDrivenModule =
    module {
        factory {
            RealProjectsDb(
                projectEntityQueries = get(),
                ioDispatcher = getIoDispatcher(),
            )
        } bind ProjectsDb::class
        factoryOf(::RealProjectsApi) bind ProjectsApi::class
        factoryOf(::ProjectSyncHandler) bind SyncOperationHandler::class
        factory {
            RealProjectsSync(
                syncEnqueuer = SyncEnqueuer(
                    enqueueSyncOperationUseCase = get(),
                    entityType = PROJECT_SYNC_ENTITY_TYPE,
                ),
            )
        } bind ProjectsSync::class
    }
