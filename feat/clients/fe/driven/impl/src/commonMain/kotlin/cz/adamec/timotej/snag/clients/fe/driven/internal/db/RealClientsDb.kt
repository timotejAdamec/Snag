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

import cz.adamec.timotej.snag.clients.fe.driven.internal.LH
import cz.adamec.timotej.snag.clients.fe.model.FrontendClient
import cz.adamec.timotej.snag.clients.fe.ports.ClientsDb
import cz.adamec.timotej.snag.feat.shared.database.fe.db.ClientEntity
import cz.adamec.timotej.snag.feat.shared.database.fe.db.ClientEntityQueries
import cz.adamec.timotej.snag.lib.database.fe.SqlDelightEntityDb
import kotlinx.coroutines.CoroutineDispatcher
import kotlin.uuid.Uuid

@Suppress("TooManyFunctions")
internal class RealClientsDb(
    private val queries: ClientEntityQueries,
    ioDispatcher: CoroutineDispatcher,
) : SqlDelightEntityDb<ClientEntity, FrontendClient>(ioDispatcher, LH.logger, "client"),
    ClientsDb {
    override fun selectAllQuery() = queries.selectAll()

    override fun selectByIdQuery(id: String) = queries.selectById(id)

    override suspend fun saveEntity(entity: ClientEntity) {
        queries.save(entity)
    }

    override suspend fun deleteEntityById(id: String) {
        queries.deleteById(id)
    }

    override suspend fun runInTransaction(block: suspend () -> Unit) {
        queries.transaction { block() }
    }

    override fun mapToModel(entity: ClientEntity) = entity.toModel()

    override fun mapToEntity(model: FrontendClient) = model.toEntity()

    override fun getAllClientsFlow() = allEntitiesFlow()

    override fun getClientFlow(id: Uuid) = entityByIdFlow(id)

    override suspend fun saveClient(client: FrontendClient) = saveOne(client)

    override suspend fun saveClients(clients: List<FrontendClient>) = saveMany(clients)

    override suspend fun deleteClient(id: Uuid) = deleteById(id)
}
