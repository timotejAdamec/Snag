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
import kotlin.uuid.Uuid

class FakeClientsApi : ClientsApi {
    private val clients = mutableMapOf<Uuid, FrontendClient>()
    var forcedFailure: OnlineDataResult.Failure? = null
    var saveClientResponseOverride: ((FrontendClient) -> OnlineDataResult<FrontendClient?>)? = null
    var modifiedSinceResults: List<ClientSyncResult> = emptyList()

    override suspend fun getClients(): OnlineDataResult<List<FrontendClient>> {
        val failure = forcedFailure
        if (failure != null) return failure
        return OnlineDataResult.Success(clients.values.toList())
    }

    override suspend fun getClient(id: Uuid): OnlineDataResult<FrontendClient> {
        val failure = forcedFailure
        if (failure != null) return failure
        return clients[id]?.let { OnlineDataResult.Success(it) }
            ?: OnlineDataResult.Failure.ProgrammerError(Exception("Not found"))
    }

    override suspend fun saveClient(client: FrontendClient): OnlineDataResult<FrontendClient?> {
        val failure = forcedFailure
        if (failure != null) return failure
        val override = saveClientResponseOverride
        return if (override != null) {
            override(client)
        } else {
            clients[client.client.id] = client
            OnlineDataResult.Success(client)
        }
    }

    override suspend fun deleteClient(
        id: Uuid,
        deletedAt: Timestamp,
    ): OnlineDataResult<Unit> {
        val failure = forcedFailure
        if (failure != null) return failure
        clients.remove(id)
        return OnlineDataResult.Success(Unit)
    }

    override suspend fun getClientsModifiedSince(since: Timestamp): OnlineDataResult<List<ClientSyncResult>> {
        val failure = forcedFailure
        if (failure != null) return failure
        return OnlineDataResult.Success(modifiedSinceResults)
    }

    fun setClient(client: FrontendClient) {
        clients[client.client.id] = client
    }
}
