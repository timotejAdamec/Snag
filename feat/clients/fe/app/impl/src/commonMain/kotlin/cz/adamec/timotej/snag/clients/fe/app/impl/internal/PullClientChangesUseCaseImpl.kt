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
import cz.adamec.timotej.snag.clients.fe.app.impl.internal.sync.CLIENT_SYNC_ENTITY_TYPE
import cz.adamec.timotej.snag.clients.fe.ports.ClientSyncResult
import cz.adamec.timotej.snag.clients.fe.ports.ClientsApi
import cz.adamec.timotej.snag.clients.fe.ports.ClientsDb
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.common.TimestampProvider
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.lib.sync.fe.app.api.GetLastPullSyncedAtTimestampUseCase
import cz.adamec.timotej.snag.lib.sync.fe.app.api.PullSyncTracker
import cz.adamec.timotej.snag.lib.sync.fe.app.api.SetLastPullSyncedAtTimestampUseCase
import cz.adamec.timotej.snag.lib.sync.fe.app.api.SyncCoordinator

internal class PullClientChangesUseCaseImpl(
    private val clientsApi: ClientsApi,
    private val clientsDb: ClientsDb,
    private val getLastPullSyncedAtTimestampUseCase: GetLastPullSyncedAtTimestampUseCase,
    private val setLastPullSyncedAtTimestampUseCase: SetLastPullSyncedAtTimestampUseCase,
    private val syncCoordinator: SyncCoordinator,
    private val timestampProvider: TimestampProvider,
    private val pullSyncTracker: PullSyncTracker,
) : PullClientChangesUseCase {
    @Suppress("LabeledExpression")
    override suspend operator fun invoke() =
        pullSyncTracker.track {
            LH.logger.d { "Starting pull sync for clients." }
            syncCoordinator.withFlushedQueue { wasFlushingSuccessful ->
                if (!wasFlushingSuccessful) {
                    LH.logger.w { "Flushing sync queue was not successful, skipping pull sync for clients." }
                    return@withFlushedQueue
                }
                val since = getLastPullSyncedAtTimestampUseCase(CLIENT_SYNC_ENTITY_TYPE) ?: Timestamp(0)
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
                                    LH.logger.d { "Processing updated client ${syncResult.client.id}." }
                                    clientsDb.saveClient(syncResult.client)
                                }
                            }
                        }
                        setLastPullSyncedAtTimestampUseCase(
                            entityType = CLIENT_SYNC_ENTITY_TYPE,
                            timestamp = now,
                        )
                        LH.logger.d { "Pull sync for clients completed, updated lastSyncedAt=$now." }
                    }
                }
            }
        }
}
