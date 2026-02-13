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

import cz.adamec.timotej.snag.feat.structures.fe.model.FrontendStructure
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.network.fe.SnagNetworkHttpClient
import cz.adamec.timotej.snag.network.fe.safeApiCall
import cz.adamec.timotej.snag.structures.be.driving.contract.DeleteStructureApiDto
import cz.adamec.timotej.snag.structures.be.driving.contract.StructureApiDto
import cz.adamec.timotej.snag.structures.fe.driven.internal.LH
import cz.adamec.timotej.snag.structures.fe.ports.StructureSyncResult
import cz.adamec.timotej.snag.structures.fe.ports.StructuresApi
import io.ktor.client.call.body
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import kotlin.uuid.Uuid

internal class RealStructuresApi(
    private val httpClient: SnagNetworkHttpClient,
) : StructuresApi {
    override suspend fun getStructures(projectId: Uuid): OnlineDataResult<List<FrontendStructure>> {
        LH.logger.d { "Fetching structures for project $projectId..." }
        return safeApiCall(logger = LH.logger, errorContext = "Error fetching structures for project $projectId.") {
            httpClient.get("/projects/$projectId/structures").body<List<StructureApiDto>>().map {
                it.toModel()
            }
        }.also { if (it is OnlineDataResult.Success) LH.logger.d { "Fetched ${it.data.size} structures for project $projectId." } }
    }

    override suspend fun deleteStructure(
        id: Uuid,
        deletedAt: Timestamp,
    ): OnlineDataResult<Unit> {
        LH.logger.d { "Deleting structure $id from API..." }
        return safeApiCall(logger = LH.logger, errorContext = "Error deleting structure $id from API.") {
            httpClient.delete("/structures/$id") {
                setBody(DeleteStructureApiDto(deletedAt = deletedAt))
            }
            Unit
        }.also { if (it is OnlineDataResult.Success) LH.logger.d { "Deleted structure $id from API." } }
    }

    override suspend fun saveStructure(frontendStructure: FrontendStructure): OnlineDataResult<FrontendStructure?> {
        LH.logger.d { "Saving structure ${frontendStructure.structure.id} to API..." }
        return safeApiCall(logger = LH.logger, errorContext = "Error saving structure ${frontendStructure.structure.id} to API.") {
            val structureDto = frontendStructure.toPutApiDto()
            val response =
                httpClient.put("/projects/${frontendStructure.structure.projectId}/structures/${frontendStructure.structure.id}") {
                    setBody(structureDto)
                }
            if (response.status != HttpStatusCode.NoContent) {
                response.body<StructureApiDto>().toModel()
            } else {
                null
            }
        }.also { if (it is OnlineDataResult.Success) LH.logger.d { "Saved structure ${frontendStructure.structure.id} to API." } }
    }

    override suspend fun getStructuresModifiedSince(
        projectId: Uuid,
        since: Timestamp,
    ): OnlineDataResult<List<StructureSyncResult>> {
        LH.logger.d { "Fetching structures modified since $since for project $projectId..." }
        return safeApiCall(logger = LH.logger, errorContext = "Error fetching structures modified since $since for project $projectId.") {
            httpClient.get("/projects/$projectId/structures?since=${since.value}").body<List<StructureApiDto>>().map { dto ->
                if (dto.deletedAt != null) {
                    StructureSyncResult.Deleted(id = dto.id)
                } else {
                    StructureSyncResult.Updated(structure = dto.toModel())
                }
            }
        }.also { if (it is OnlineDataResult.Success) LH.logger.d { "Fetched ${it.data.size} modified structures for project $projectId." } }
    }
}
