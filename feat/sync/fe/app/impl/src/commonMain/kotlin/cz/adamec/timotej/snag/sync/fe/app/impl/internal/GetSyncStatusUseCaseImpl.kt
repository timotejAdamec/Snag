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

package cz.adamec.timotej.snag.sync.fe.app.impl.internal

import cz.adamec.timotej.snag.core.network.fe.ConnectionStatusProvider
import cz.adamec.timotej.snag.sync.fe.app.api.GetSyncStatusUseCase
import cz.adamec.timotej.snag.sync.fe.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

internal class GetSyncStatusUseCaseImpl(
    private val pushSyncEngine: PushSyncEngine,
    private val connectionStatusProvider: ConnectionStatusProvider,
    private val pullSyncEngine: PullSyncEngine,
) : GetSyncStatusUseCase {
    override fun invoke(): Flow<SyncStatus> =
        combine(
            pushSyncEngine.status,
            pullSyncEngine.status,
            connectionStatusProvider.isConnectedFlow(),
        ) { pushStatus, pullStatus, isConnected ->
            when {
                !isConnected -> SyncStatus.Offline
                pushStatus is PushSyncEngineStatus.Pushing -> SyncStatus.Syncing
                pullStatus is PullSyncEngineStatus.Pulling -> SyncStatus.Syncing
                pushStatus is PushSyncEngineStatus.Failed -> SyncStatus.Error
                pullStatus is PullSyncEngineStatus.Failed -> SyncStatus.Error
                else -> SyncStatus.Synced
            }
        }
}
