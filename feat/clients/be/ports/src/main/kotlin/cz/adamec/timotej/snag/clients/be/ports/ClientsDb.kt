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

package cz.adamec.timotej.snag.clients.be.ports

import cz.adamec.timotej.snag.clients.be.model.BackendClient
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import kotlin.uuid.Uuid

interface ClientsDb {
    suspend fun getClients(): List<BackendClient>

    suspend fun getClient(id: Uuid): BackendClient?

    suspend fun saveClient(client: BackendClient): BackendClient?

    suspend fun deleteClient(
        id: Uuid,
        deletedAt: Timestamp,
    ): BackendClient?

    suspend fun getClientsModifiedSince(since: Timestamp): List<BackendClient>
}
