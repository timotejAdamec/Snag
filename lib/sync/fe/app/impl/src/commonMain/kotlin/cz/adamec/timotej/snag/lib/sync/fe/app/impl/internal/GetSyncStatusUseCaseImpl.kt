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

package cz.adamec.timotej.snag.lib.sync.fe.app.impl.internal

import cz.adamec.timotej.snag.lib.sync.fe.app.api.GetSyncStatusUseCase
import cz.adamec.timotej.snag.lib.sync.fe.app.api.SyncStatus
import cz.adamec.timotej.snag.network.fe.InternetConnectionStatusListener
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

internal class GetSyncStatusUseCaseImpl(
    private val syncEngine: SyncEngine,
    private val connectionStatusListener: InternetConnectionStatusListener,
) : GetSyncStatusUseCase {
    override fun invoke(): Flow<SyncStatus> =
        combine(
            syncEngine.status,
            connectionStatusListener.isConnectedFlow(),
        ) { engineStatus, isConnected ->
            when {
                !isConnected -> SyncStatus.Offline
                engineStatus is SyncEngineStatus.Syncing -> SyncStatus.Syncing
                engineStatus is SyncEngineStatus.Failed -> SyncStatus.Error
                else -> SyncStatus.Synced
            }
        }
}
