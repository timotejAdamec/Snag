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

package cz.adamec.timotej.snag.clients.fe.ports

import cz.adamec.timotej.snag.clients.fe.model.FrontendClient
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import kotlin.uuid.Uuid

sealed interface ClientSyncResult {
    data class Deleted(
        val id: Uuid,
    ) : ClientSyncResult

    data class Updated(
        val client: FrontendClient,
    ) : ClientSyncResult
}

interface ClientsApi {
    suspend fun getClients(): OnlineDataResult<List<FrontendClient>>

    suspend fun getClient(id: Uuid): OnlineDataResult<FrontendClient>

    suspend fun saveClient(client: FrontendClient): OnlineDataResult<FrontendClient?>

    suspend fun deleteClient(
        id: Uuid,
        deletedAt: Timestamp,
    ): OnlineDataResult<Unit>

    suspend fun getClientsModifiedSince(since: Timestamp): OnlineDataResult<List<ClientSyncResult>>
}
