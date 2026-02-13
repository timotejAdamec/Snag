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
import cz.adamec.timotej.snag.lib.database.fe.test.FakeDbOps
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

class FakeClientsDb : ClientsDb {
    private val ops = FakeDbOps<FrontendClient>(getId = { it.client.id })

    var forcedFailure
        get() = ops.forcedFailure
        set(value) {
            ops.forcedFailure = value
        }

    override fun getAllClientsFlow(): Flow<OfflineFirstDataResult<List<FrontendClient>>> = ops.allItemsFlow()

    override fun getClientFlow(id: Uuid): Flow<OfflineFirstDataResult<FrontendClient?>> = ops.itemByIdFlow(id)

    override suspend fun saveClient(client: FrontendClient): OfflineFirstDataResult<Unit> = ops.saveOneItem(client)

    override suspend fun saveClients(clients: List<FrontendClient>): OfflineFirstDataResult<Unit> = ops.saveManyItems(clients)

    override suspend fun deleteClient(id: Uuid): OfflineFirstDataResult<Unit> = ops.deleteItem(id)

    fun setClient(client: FrontendClient) = ops.setItem(client)
}
