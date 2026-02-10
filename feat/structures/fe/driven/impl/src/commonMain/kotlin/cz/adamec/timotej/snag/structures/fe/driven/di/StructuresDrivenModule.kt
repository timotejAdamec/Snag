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

package cz.adamec.timotej.snag.structures.fe.driven.di

import cz.adamec.timotej.snag.lib.core.common.di.getIoDispatcher
import cz.adamec.timotej.snag.lib.sync.fe.app.api.handler.SyncOperationHandler
import cz.adamec.timotej.snag.structures.fe.driven.internal.api.RealStructuresApi
import cz.adamec.timotej.snag.structures.fe.driven.internal.db.RealStructuresDb
import cz.adamec.timotej.snag.structures.fe.driven.internal.db.StructuresSqlDelightDbOps
import cz.adamec.timotej.snag.structures.fe.driven.internal.storage.RealStructuresFileStorage
import cz.adamec.timotej.snag.structures.fe.driven.internal.sync.RealStructuresPullSyncCoordinator
import cz.adamec.timotej.snag.structures.fe.driven.internal.sync.RealStructuresPullSyncTimestampDataSource
import cz.adamec.timotej.snag.structures.fe.driven.internal.sync.RealStructuresSync
import cz.adamec.timotej.snag.structures.fe.driven.internal.sync.StructureSyncHandler
import cz.adamec.timotej.snag.structures.fe.ports.StructuresApi
import cz.adamec.timotej.snag.structures.fe.ports.StructuresDb
import cz.adamec.timotej.snag.structures.fe.ports.StructuresFileStorage
import cz.adamec.timotej.snag.structures.fe.ports.StructuresPullSyncCoordinator
import cz.adamec.timotej.snag.structures.fe.ports.StructuresPullSyncTimestampDataSource
import cz.adamec.timotej.snag.structures.fe.ports.StructuresSync
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val structuresDrivenModule =
    module {
        factory {
            StructuresSqlDelightDbOps(
                queries = get(),
                ioDispatcher = getIoDispatcher(),
            )
        }
        factory { RealStructuresDb(ops = get()) } bind StructuresDb::class
        factoryOf(::RealStructuresApi) bind StructuresApi::class
        factoryOf(::StructureSyncHandler) bind SyncOperationHandler::class
        factoryOf(::RealStructuresSync) bind StructuresSync::class
        factory {
            RealStructuresPullSyncTimestampDataSource(
                queries = get(),
                ioDispatcher = getIoDispatcher(),
            )
        } bind StructuresPullSyncTimestampDataSource::class
        factoryOf(::RealStructuresPullSyncCoordinator) bind StructuresPullSyncCoordinator::class
        factoryOf(::RealStructuresFileStorage) bind StructuresFileStorage::class
    }
