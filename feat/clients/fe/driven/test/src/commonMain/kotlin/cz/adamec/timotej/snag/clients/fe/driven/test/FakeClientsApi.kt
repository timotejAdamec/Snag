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
import cz.adamec.timotej.snag.lib.core.fe.test.FakeEntityApi
import kotlin.uuid.Uuid

class FakeClientsApi :
    FakeEntityApi<FrontendClient, ClientSyncResult>(
        getId = { it.client.id },
    ),
    ClientsApi {
    var saveClientResponseOverride
        get() = saveResponseOverride
        set(value) {
            saveResponseOverride = value
        }

    override suspend fun getClients() = getAllItems()

    override suspend fun getClient(id: Uuid) = getItemById(id)

    override suspend fun saveClient(client: FrontendClient) = saveItem(client)

    override suspend fun deleteClient(
        id: Uuid,
        deletedAt: Timestamp,
    ) = deleteItemById(id)

    override suspend fun getClientsModifiedSince(since: Timestamp) = getModifiedSinceItems()

    fun setClient(client: FrontendClient) = setItem(client)
}
