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

package cz.adamec.timotej.snag.structures.fe.driven.internal.api

import cz.adamec.timotej.snag.feat.structures.business.Structure
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.network.fe.SnagNetworkHttpClient
import cz.adamec.timotej.snag.network.fe.safeApiCall
import cz.adamec.timotej.snag.structures.be.driving.contract.StructureApiDto
import cz.adamec.timotej.snag.structures.fe.driven.internal.LH
import cz.adamec.timotej.snag.structures.fe.ports.StructuresApi
import io.ktor.client.call.body
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import kotlin.uuid.Uuid

internal class RealStructuresApi(
    private val httpClient: SnagNetworkHttpClient,
) : StructuresApi {
    override suspend fun getStructures(projectId: Uuid): OnlineDataResult<List<Structure>> {
        LH.logger.d { "Fetching structures for project $projectId..." }
        return safeApiCall(LH.logger, "Error fetching structures for project $projectId.") {
            httpClient.get("/projects/$projectId/structures").body<List<StructureApiDto>>().map {
                it.toBusiness()
            }
        }.also { if (it is OnlineDataResult.Success) LH.logger.d { "Fetched ${it.data.size} structures for project $projectId." } }
    }

    override suspend fun deleteStructure(id: Uuid): OnlineDataResult<Unit> =
        safeApiCall(LH.logger, "Error deleting structure $id from API.") {
            httpClient.delete("/structures/$id")
            Unit
        }.also { if (it is OnlineDataResult.Success) LH.logger.d { "Deleted structure $id from API." } }

    override suspend fun saveStructure(structure: Structure): OnlineDataResult<Structure?> {
        LH.logger.d { "Saving structure ${structure.id} to API..." }
        return safeApiCall(LH.logger, "Error saving structure ${structure.id} to API.") {
            val structureDto = structure.toPutApiDto()
            val response =
                httpClient.put("/projects/${structure.projectId}/structures/${structure.id}") {
                    setBody(structureDto)
                }
            if (response.status != HttpStatusCode.NoContent) {
                response.body<StructureApiDto>().toBusiness()
            } else {
                null
            }
        }.also { if (it is OnlineDataResult.Success) LH.logger.d { "Saved structure ${structure.id} to API." } }
    }
}
