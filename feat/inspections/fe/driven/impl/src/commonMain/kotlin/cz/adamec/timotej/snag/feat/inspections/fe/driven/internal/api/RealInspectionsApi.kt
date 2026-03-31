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

package cz.adamec.timotej.snag.feat.inspections.fe.driven.internal.api

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.core.network.fe.safeApiCall
import cz.adamec.timotej.snag.feat.inspections.app.model.AppInspection
import cz.adamec.timotej.snag.feat.inspections.contract.DeleteInspectionApiDto
import cz.adamec.timotej.snag.feat.inspections.contract.InspectionApiDto
import cz.adamec.timotej.snag.feat.inspections.fe.driven.internal.LH
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionSyncResult
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsApi
import cz.adamec.timotej.snag.network.fe.SnagNetworkHttpClient
import io.ktor.client.call.body
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import kotlin.uuid.Uuid

internal class RealInspectionsApi(
    private val httpClient: SnagNetworkHttpClient,
) : InspectionsApi {
    override suspend fun getInspections(projectId: Uuid): OnlineDataResult<List<AppInspection>> {
        LH.logger.d { "Fetching inspections for project $projectId..." }
        val result =
            safeApiCall(logger = LH.logger, errorContext = "Error fetching inspections for project $projectId.") {
                httpClient.get("/projects/$projectId/inspections").body<List<InspectionApiDto>>().map {
                    it.toModel()
                }
            }
        if (result is OnlineDataResult.Success) {
            LH.logger.d { "Fetched ${result.data.size} inspections for project $projectId." }
        }
        return result
    }

    override suspend fun deleteInspection(
        id: Uuid,
        deletedAt: Timestamp,
    ): OnlineDataResult<AppInspection?> {
        LH.logger.d { "Deleting inspection $id from API..." }
        val result =
            safeApiCall(logger = LH.logger, errorContext = "Error deleting inspection $id from API.") {
                val response =
                    httpClient.delete("/inspections/$id") {
                        setBody(DeleteInspectionApiDto(deletedAt = deletedAt))
                    }
                if (response.status != HttpStatusCode.NoContent) {
                    response.body<InspectionApiDto>().toModel()
                } else {
                    null
                }
            }
        if (result is OnlineDataResult.Success) {
            LH.logger.d { "Deleted inspection $id from API." }
        }
        return result
    }

    override suspend fun saveInspection(frontendInspection: AppInspection): OnlineDataResult<AppInspection?> {
        LH.logger.d { "Saving inspection ${frontendInspection.id} to API..." }
        val result =
            safeApiCall(
                logger = LH.logger,
                errorContext = "Error saving inspection ${frontendInspection.id} to API.",
            ) {
                val inspectionDto = frontendInspection.toPutApiDto()
                val response =
                    httpClient.put(
                        "/projects/${frontendInspection.projectId}/inspections/${frontendInspection.id}",
                    ) {
                        setBody(inspectionDto)
                    }
                if (response.status != HttpStatusCode.NoContent) {
                    response.body<InspectionApiDto>().toModel()
                } else {
                    null
                }
            }
        if (result is OnlineDataResult.Success) {
            LH.logger.d { "Saved inspection ${frontendInspection.id} to API." }
        }
        return result
    }

    override suspend fun getInspectionsModifiedSince(
        projectId: Uuid,
        since: Timestamp,
    ): OnlineDataResult<List<InspectionSyncResult>> {
        LH.logger.d { "Fetching inspections modified since $since for project $projectId..." }
        val result =
            safeApiCall(
                logger = LH.logger,
                errorContext = "Error fetching inspections modified since $since for project $projectId.",
            ) {
                val response = httpClient.get("/projects/$projectId/inspections?since=${since.value}")
                val dtos = response.body<List<InspectionApiDto>>()
                dtos.map { dto ->
                    if (dto.deletedAt != null) {
                        InspectionSyncResult.Deleted(id = dto.id)
                    } else {
                        InspectionSyncResult.Updated(inspection = dto.toModel())
                    }
                }
            }
        if (result is OnlineDataResult.Success) {
            LH.logger.d { "Fetched ${result.data.size} modified inspections for project $projectId." }
        }
        return result
    }
}
