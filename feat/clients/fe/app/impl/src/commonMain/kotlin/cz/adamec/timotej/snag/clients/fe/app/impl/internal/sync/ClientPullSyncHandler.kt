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

package cz.adamec.timotej.snag.clients.fe.app.impl.internal.sync

import cz.adamec.timotej.snag.clients.fe.app.impl.internal.LH
import cz.adamec.timotej.snag.clients.fe.ports.ClientSyncResult
import cz.adamec.timotej.snag.clients.fe.ports.ClientsApi
import cz.adamec.timotej.snag.clients.fe.ports.ClientsDb
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.foundation.common.TimestampProvider
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.sync.fe.app.api.GetLastPullSyncedAtTimestampUseCase
import cz.adamec.timotej.snag.sync.fe.app.api.SetLastPullSyncedAtTimestampUseCase
import cz.adamec.timotej.snag.sync.fe.app.api.handler.DbApiPullSyncHandler

internal class ClientPullSyncHandler(
    private val clientsApi: ClientsApi,
    private val clientsDb: ClientsDb,
    getLastPullSyncedAtTimestampUseCase: GetLastPullSyncedAtTimestampUseCase,
    setLastPullSyncedAtTimestampUseCase: SetLastPullSyncedAtTimestampUseCase,
    timestampProvider: TimestampProvider,
) : DbApiPullSyncHandler<ClientSyncResult>(
    logger = LH.logger,
    timestampProvider = timestampProvider,
    getLastPullSyncedAtTimestampUseCase = getLastPullSyncedAtTimestampUseCase,
    setLastPullSyncedAtTimestampUseCase = setLastPullSyncedAtTimestampUseCase,
) {
    override val entityTypeId: String = CLIENT_SYNC_ENTITY_TYPE
    override val entityName: String = "client"

    override suspend fun fetchChangesFromApi(
        scopeId: String,
        since: Timestamp,
    ): OnlineDataResult<List<ClientSyncResult>> = clientsApi.getClientsModifiedSince(since)

    override suspend fun applyChange(change: ClientSyncResult) {
        when (change) {
            is ClientSyncResult.Deleted -> {
                LH.logger.d { "Processing deleted client ${change.id}." }
                clientsDb.deleteClient(change.id)
            }
            is ClientSyncResult.Updated -> {
                LH.logger.d { "Processing updated client ${change.client.client.id}." }
                clientsDb.saveClient(change.client)
            }
        }
    }
}
