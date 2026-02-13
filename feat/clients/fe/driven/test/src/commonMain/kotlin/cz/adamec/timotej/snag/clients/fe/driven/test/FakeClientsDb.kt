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
import cz.adamec.timotej.snag.lib.core.fe.test.FakeEntityDb
import kotlin.uuid.Uuid

class FakeClientsDb :
    FakeEntityDb<FrontendClient>(
        getId = { it.client.id },
    ),
    ClientsDb {
    override fun getAllClientsFlow() = allItemsFlow()

    override fun getClientFlow(id: Uuid) = itemByIdFlow(id)

    override suspend fun saveClient(client: FrontendClient) = saveOneItem(client)

    override suspend fun saveClients(clients: List<FrontendClient>) = saveManyItems(clients)

    override suspend fun deleteClient(id: Uuid) = deleteItem(id)

    fun setClient(client: FrontendClient) = setItem(client)
}
