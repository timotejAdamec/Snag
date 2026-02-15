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

package cz.adamec.timotej.snag.clients.fe.driven.test

import cz.adamec.timotej.snag.clients.fe.model.FrontendClient
import cz.adamec.timotej.snag.clients.fe.ports.ClientSyncResult
import cz.adamec.timotej.snag.clients.fe.ports.ClientsApi
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.network.fe.test.FakeApiOps
import kotlin.uuid.Uuid

class FakeClientsApi : ClientsApi {
    private val ops = FakeApiOps<FrontendClient, ClientSyncResult>(getId = { it.client.id })

    var forcedFailure
        get() = ops.forcedFailure
        set(value) {
            ops.forcedFailure = value
        }

    var saveClientResponseOverride
        get() = ops.saveResponseOverride
        set(value) {
            ops.saveResponseOverride = value
        }

    var modifiedSinceResults
        get() = ops.modifiedSinceResults
        set(value) {
            ops.modifiedSinceResults = value
        }

    override suspend fun getClients(): OnlineDataResult<List<FrontendClient>> = ops.getAllItems()

    override suspend fun getClient(id: Uuid): OnlineDataResult<FrontendClient> = ops.getItemById(id)

    override suspend fun saveClient(client: FrontendClient): OnlineDataResult<FrontendClient?> = ops.saveItem(client)

    override suspend fun deleteClient(
        id: Uuid,
        deletedAt: Timestamp,
    ): OnlineDataResult<Unit> = ops.deleteItemById(id)

    override suspend fun getClientsModifiedSince(since: Timestamp): OnlineDataResult<List<ClientSyncResult>> = ops.getModifiedSinceItems()

    fun setClient(client: FrontendClient) = ops.setItem(client)
}
