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

package cz.adamec.timotej.snag.clients.fe.driven.internal.api

import cz.adamec.timotej.snag.clients.be.driving.contract.ClientApiDto
import cz.adamec.timotej.snag.clients.be.driving.contract.DeleteClientApiDto
import cz.adamec.timotej.snag.clients.fe.driven.internal.LH
import cz.adamec.timotej.snag.clients.fe.model.FrontendClient
import cz.adamec.timotej.snag.clients.fe.ports.ClientSyncResult
import cz.adamec.timotej.snag.clients.fe.ports.ClientsApi
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.network.fe.SnagNetworkHttpClient
import cz.adamec.timotej.snag.network.fe.safeApiCall
import io.ktor.client.call.body
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import kotlin.uuid.Uuid

internal class RealClientsApi(
    private val httpClient: SnagNetworkHttpClient,
) : ClientsApi {
    override suspend fun getClients(): OnlineDataResult<List<FrontendClient>> {
        LH.logger.d { "Fetching clients..." }
        return safeApiCall(logger = LH.logger, errorContext = "Error fetching clients.") {
            httpClient.get("/clients").body<List<ClientApiDto>>().map {
                it.toModel()
            }
        }.also { if (it is OnlineDataResult.Success) LH.logger.d { "Fetched ${it.data.size} clients." } }
    }

    override suspend fun getClient(id: Uuid): OnlineDataResult<FrontendClient> {
        LH.logger.d { "Fetching client $id..." }
        return safeApiCall(logger = LH.logger, errorContext = "Error fetching client $id.") {
            httpClient.get("/clients/$id").body<ClientApiDto>().toModel()
        }.also { if (it is OnlineDataResult.Success) LH.logger.d { "Fetched client $id." } }
    }

    override suspend fun saveClient(client: FrontendClient): OnlineDataResult<FrontendClient?> {
        LH.logger.d { "Saving client ${client.client.id} to API..." }
        return safeApiCall(logger = LH.logger, errorContext = "Error saving client ${client.client.id} to API.") {
            val clientDto = client.toPutApiDto()
            val response =
                httpClient.put("/clients/${client.client.id}") {
                    setBody(clientDto)
                }
            if (response.status != HttpStatusCode.NoContent) {
                response.body<ClientApiDto>().toModel()
            } else {
                null
            }
        }.also { if (it is OnlineDataResult.Success) LH.logger.d { "Saved client ${client.client.id} to API." } }
    }

    override suspend fun deleteClient(
        id: Uuid,
        deletedAt: Timestamp,
    ): OnlineDataResult<Unit> {
        LH.logger.d { "Deleting client $id from API..." }
        return safeApiCall(logger = LH.logger, errorContext = "Error deleting client $id from API.") {
            httpClient.delete("/clients/$id") {
                setBody(DeleteClientApiDto(deletedAt = deletedAt))
            }
            Unit
        }.also { if (it is OnlineDataResult.Success) LH.logger.d { "Deleted client $id from API." } }
    }

    override suspend fun getClientsModifiedSince(since: Timestamp): OnlineDataResult<List<ClientSyncResult>> {
        LH.logger.d { "Fetching clients modified since $since..." }
        return safeApiCall(logger = LH.logger, errorContext = "Error fetching clients modified since $since.") {
            httpClient.get("/clients?since=${since.value}").body<List<ClientApiDto>>().map { dto ->
                if (dto.deletedAt != null) {
                    ClientSyncResult.Deleted(id = dto.id)
                } else {
                    ClientSyncResult.Updated(client = dto.toModel())
                }
            }
        }.also { if (it is OnlineDataResult.Success) LH.logger.d { "Fetched ${it.data.size} modified clients." } }
    }
}
