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

package cz.adamec.timotej.snag.clients.fe.driven.internal.sync

import cz.adamec.timotej.snag.clients.fe.driven.internal.LH
import cz.adamec.timotej.snag.clients.fe.model.FrontendClient
import cz.adamec.timotej.snag.clients.fe.ports.ClientsApi
import cz.adamec.timotej.snag.clients.fe.ports.ClientsDb
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.common.TimestampProvider
import cz.adamec.timotej.snag.lib.sync.fe.app.api.handler.DbApiSyncHandler
import kotlin.uuid.Uuid

internal class ClientSyncHandler(
    private val clientsApi: ClientsApi,
    private val clientsDb: ClientsDb,
    timestampProvider: TimestampProvider,
) : DbApiSyncHandler<FrontendClient>(LH.logger, timestampProvider) {
    override val entityTypeId: String = CLIENT_SYNC_ENTITY_TYPE
    override val entityName: String = "client"

    override fun getEntityFlow(entityId: Uuid) = clientsDb.getClientFlow(entityId)

    override suspend fun saveEntityToApi(entity: FrontendClient) = clientsApi.saveClient(entity)

    override suspend fun deleteEntityFromApi(
        entityId: Uuid,
        deletedAt: Timestamp,
    ) = clientsApi.deleteClient(entityId, deletedAt)

    override suspend fun saveEntityToDb(entity: FrontendClient) = clientsDb.saveClient(entity)
}
