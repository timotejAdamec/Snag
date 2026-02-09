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

package cz.adamec.timotej.snag.lib.sync.fe.app.impl.di

import cz.adamec.timotej.snag.lib.sync.fe.app.api.EnqueueSyncDeleteUseCase
import cz.adamec.timotej.snag.lib.sync.fe.app.api.EnqueueSyncSaveUseCase
import cz.adamec.timotej.snag.lib.sync.fe.app.api.GetSyncStatusUseCase
import cz.adamec.timotej.snag.lib.sync.fe.app.api.SyncCoordinator
import cz.adamec.timotej.snag.lib.sync.fe.app.api.handler.SyncOperationHandler
import cz.adamec.timotej.snag.lib.sync.fe.app.impl.internal.EnqueueSyncDeleteUseCaseImpl
import cz.adamec.timotej.snag.lib.sync.fe.app.impl.internal.EnqueueSyncOperationUseCase
import cz.adamec.timotej.snag.lib.sync.fe.app.impl.internal.EnqueueSyncSaveUseCaseImpl
import cz.adamec.timotej.snag.lib.sync.fe.app.impl.internal.GetSyncStatusUseCaseImpl
import cz.adamec.timotej.snag.lib.sync.fe.app.impl.internal.SyncEngine
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.binds
import org.koin.dsl.module

val syncAppModule =
    module {
        single {
            SyncEngine(
                syncQueue = get(),
                handlers = getAll<SyncOperationHandler>(),
                applicationScope = get(),
            )
        } binds arrayOf(EnqueueSyncOperationUseCase::class, SyncCoordinator::class)
        singleOf(::EnqueueSyncSaveUseCaseImpl) bind EnqueueSyncSaveUseCase::class
        singleOf(::EnqueueSyncDeleteUseCaseImpl) bind EnqueueSyncDeleteUseCase::class
        singleOf(::GetSyncStatusUseCaseImpl) bind GetSyncStatusUseCase::class
    }
