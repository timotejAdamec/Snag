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
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

interface ClientsDb {
    fun getAllClientsFlow(): Flow<OfflineFirstDataResult<List<FrontendClient>>>

    suspend fun saveClients(clients: List<FrontendClient>): OfflineFirstDataResult<Unit>

    fun getClientFlow(id: Uuid): Flow<OfflineFirstDataResult<FrontendClient?>>

    suspend fun saveClient(client: FrontendClient): OfflineFirstDataResult<Unit>

    suspend fun deleteClient(id: Uuid): OfflineFirstDataResult<Unit>
}
