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

package cz.adamec.timotej.snag.sync.fe.driven.impl.di

import cz.adamec.timotej.snag.core.foundation.common.di.getIoDispatcher
import cz.adamec.timotej.snag.lib.database.fe.sqlDelightDatabaseModule
import cz.adamec.timotej.snag.sync.fe.driven.impl.RealPullSyncTimestampDb
import cz.adamec.timotej.snag.sync.fe.driven.impl.RealSyncQueue
import cz.adamec.timotej.snag.sync.fe.driven.impl.db.PullSyncTimestampEntityQueries
import cz.adamec.timotej.snag.sync.fe.driven.impl.db.SyncDatabase
import cz.adamec.timotej.snag.sync.fe.driven.impl.db.SyncOperationEntityQueries
import cz.adamec.timotej.snag.sync.fe.ports.PullSyncTimestampDb
import cz.adamec.timotej.snag.sync.fe.ports.SyncQueue
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

private val syncDatabaseModule =
    sqlDelightDatabaseModule(
        schema = SyncDatabase.Schema,
        name = "sync.db",
        qualifier = named("syncDb"),
    )

val syncDrivenModule =
    module {
        includes(syncDatabaseModule)

        single { SyncDatabase(driver = get(named("syncDb"))) } bind SyncDatabase::class

        factory { get<SyncDatabase>().syncOperationEntityQueries } bind SyncOperationEntityQueries::class
        factory { get<SyncDatabase>().pullSyncTimestampEntityQueries } bind PullSyncTimestampEntityQueries::class

        factory {
            RealSyncQueue(
                syncOperationEntityQueries = get(),
                ioDispatcher = getIoDispatcher(),
                uuidProvider = get(),
            )
        } bind SyncQueue::class
        factory {
            RealPullSyncTimestampDb(
                queries = get(),
                ioDispatcher = getIoDispatcher(),
            )
        } bind PullSyncTimestampDb::class
    }
