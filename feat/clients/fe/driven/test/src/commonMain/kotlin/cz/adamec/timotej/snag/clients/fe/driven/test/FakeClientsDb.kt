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

import cz.adamec.timotej.snag.clients.app.model.AppClient
import cz.adamec.timotej.snag.clients.fe.ports.ClientsDb
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.database.fe.test.FakeDbOps
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

class FakeClientsDb : ClientsDb {
    private val ops = FakeDbOps<AppClient>(getId = { it.id })

    var forcedFailure
        get() = ops.forcedFailure
        set(value) {
            ops.forcedFailure = value
        }

    override fun getAllClientsFlow(): Flow<OfflineFirstDataResult<List<AppClient>>> = ops.allItemsFlow()

    override fun getClientFlow(id: Uuid): Flow<OfflineFirstDataResult<AppClient?>> = ops.itemByIdFlow(id)

    override suspend fun saveClient(client: AppClient): OfflineFirstDataResult<Unit> = ops.saveOneItem(client)

    override suspend fun saveClients(clients: List<AppClient>): OfflineFirstDataResult<Unit> = ops.saveManyItems(clients)

    override suspend fun deleteClient(id: Uuid): OfflineFirstDataResult<Unit> = ops.deleteItem(id)

    fun setClient(client: AppClient) = ops.setItem(client)
}
