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

package cz.adamec.timotej.snag.clients.fe.driven.internal.db

import cz.adamec.timotej.snag.clients.fe.model.FrontendClient
import cz.adamec.timotej.snag.clients.fe.ports.ClientsDb
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

internal class RealClientsDb(
    private val ops: ClientsSqlDelightDbOps,
) : ClientsDb {
    override fun getAllClientsFlow(): Flow<OfflineFirstDataResult<List<FrontendClient>>> = ops.allEntitiesFlow()

    override fun getClientFlow(id: Uuid): Flow<OfflineFirstDataResult<FrontendClient?>> = ops.entityByIdFlow(id)

    override suspend fun saveClient(client: FrontendClient): OfflineFirstDataResult<Unit> = ops.saveOne(client)

    override suspend fun saveClients(clients: List<FrontendClient>): OfflineFirstDataResult<Unit> = ops.saveMany(clients)

    override suspend fun deleteClient(id: Uuid): OfflineFirstDataResult<Unit> = ops.deleteById(id)
}
