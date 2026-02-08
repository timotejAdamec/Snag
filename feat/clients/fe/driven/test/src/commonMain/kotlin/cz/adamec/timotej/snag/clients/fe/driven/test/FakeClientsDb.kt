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
import cz.adamec.timotej.snag.clients.fe.ports.ClientsDb
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlin.uuid.Uuid

class FakeClientsDb : ClientsDb {
    private val clients = MutableStateFlow<Map<Uuid, FrontendClient>>(emptyMap())
    var forcedFailure: OfflineFirstDataResult.ProgrammerError? = null

    override fun getAllClientsFlow(): Flow<OfflineFirstDataResult<List<FrontendClient>>> =
        clients.map { OfflineFirstDataResult.Success(it.values.toList()) }

    override suspend fun saveClients(clients: List<FrontendClient>): OfflineFirstDataResult<Unit> {
        val failure = forcedFailure
        if (failure != null) return failure

        this.clients.update { current ->
            current + clients.associateBy { it.client.id }
        }
        return OfflineFirstDataResult.Success(Unit)
    }

    override fun getClientFlow(id: Uuid): Flow<OfflineFirstDataResult<FrontendClient?>> =
        clients.map { map ->
            val failure = forcedFailure
            if (failure != null) {
                failure
            } else {
                OfflineFirstDataResult.Success(map[id])
            }
        }

    override suspend fun saveClient(client: FrontendClient): OfflineFirstDataResult<Unit> {
        val failure = forcedFailure
        if (failure != null) return failure

        clients.update { it + (client.client.id to client) }
        return OfflineFirstDataResult.Success(Unit)
    }

    override suspend fun deleteClient(id: Uuid): OfflineFirstDataResult<Unit> {
        val failure = forcedFailure
        if (failure != null) return failure

        clients.update { it - id }
        return OfflineFirstDataResult.Success(Unit)
    }

    fun setClient(client: FrontendClient) {
        clients.update { it + (client.client.id to client) }
    }
}
