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

package cz.adamec.timotej.snag.clients.fe.app.impl.internal

import cz.adamec.timotej.snag.clients.fe.app.api.PullClientChangesUseCase
import cz.adamec.timotej.snag.clients.fe.ports.ClientSyncResult
import cz.adamec.timotej.snag.clients.fe.ports.ClientsApi
import cz.adamec.timotej.snag.clients.fe.ports.ClientsDb
import cz.adamec.timotej.snag.clients.fe.ports.ClientsPullSyncCoordinator
import cz.adamec.timotej.snag.clients.fe.ports.ClientsPullSyncTimestampDataSource
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.common.TimestampProvider
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult

internal class PullClientChangesUseCaseImpl(
    private val clientsApi: ClientsApi,
    private val clientsDb: ClientsDb,
    private val clientsPullSyncTimestampDataSource: ClientsPullSyncTimestampDataSource,
    private val clientsPullSyncCoordinator: ClientsPullSyncCoordinator,
    private val timestampProvider: TimestampProvider,
) : PullClientChangesUseCase {
    override suspend operator fun invoke() {
        LH.logger.d { "Starting pull sync for clients." }
        clientsPullSyncCoordinator.withFlushedQueue {
            val since = clientsPullSyncTimestampDataSource.getLastSyncedAt() ?: Timestamp(0)
            val now = timestampProvider.getNowTimestamp()
            LH.logger.d { "Pulling client changes since=$since, now=$now." }

            when (val result = clientsApi.getClientsModifiedSince(since)) {
                is OnlineDataResult.Failure -> {
                    LH.logger.w { "Error pulling client changes." }
                }
                is OnlineDataResult.Success -> {
                    val changes = result.data
                    LH.logger.d { "Received ${changes.size} client change(s)." }
                    changes.forEach { syncResult ->
                        when (syncResult) {
                            is ClientSyncResult.Deleted -> {
                                LH.logger.d { "Processing deleted client ${syncResult.id}." }
                                clientsDb.deleteClient(syncResult.id)
                            }
                            is ClientSyncResult.Updated -> {
                                LH.logger.d { "Processing updated client ${syncResult.client.client.id}." }
                                clientsDb.saveClient(syncResult.client)
                            }
                        }
                    }
                    clientsPullSyncTimestampDataSource.setLastSyncedAt(now)
                    LH.logger.d { "Pull sync for clients completed, updated lastSyncedAt=$now." }
                }
            }
        }
    }
}
