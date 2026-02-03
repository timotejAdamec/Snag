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

package cz.adamec.timotej.snag.lib.sync.fe.app.di

import cz.adamec.timotej.snag.lib.sync.fe.app.EnqueueSyncDeleteUseCase
import cz.adamec.timotej.snag.lib.sync.fe.app.EnqueueSyncSaveUseCase
import cz.adamec.timotej.snag.lib.sync.fe.app.handler.SyncOperationHandler
import cz.adamec.timotej.snag.lib.sync.fe.app.internal.EnqueueSyncDeleteUseCaseImpl
import cz.adamec.timotej.snag.lib.sync.fe.app.internal.EnqueueSyncOperationUseCase
import cz.adamec.timotej.snag.lib.sync.fe.app.internal.EnqueueSyncSaveUseCaseImpl
import cz.adamec.timotej.snag.lib.sync.fe.app.internal.SyncEngine
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val syncAppModule =
    module {
        single {
            SyncEngine(
                syncQueue = get(),
                handlers = getAll<SyncOperationHandler>(),
                applicationScope = get(),
            )
        } bind EnqueueSyncOperationUseCase::class
        singleOf(::EnqueueSyncSaveUseCaseImpl) bind EnqueueSyncSaveUseCase::class
        singleOf(::EnqueueSyncDeleteUseCaseImpl) bind EnqueueSyncDeleteUseCase::class
    }
