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

package cz.adamec.timotej.snag.findings.fe.driven.internal.api

import cz.adamec.timotej.snag.feat.findings.fe.model.FrontendFinding
import cz.adamec.timotej.snag.findings.be.driving.contract.FindingApiDto
import cz.adamec.timotej.snag.findings.fe.driven.internal.LH
import cz.adamec.timotej.snag.findings.fe.ports.FindingSyncResult
import cz.adamec.timotej.snag.findings.fe.ports.FindingsApi
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.network.fe.SnagNetworkHttpClient
import cz.adamec.timotej.snag.network.fe.safeApiCall
import io.ktor.client.call.body
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import kotlin.uuid.Uuid

internal class RealFindingsApi(
    private val httpClient: SnagNetworkHttpClient,
) : FindingsApi {
    override suspend fun getFindings(structureId: Uuid): OnlineDataResult<List<FrontendFinding>> {
        LH.logger.d { "Fetching findings for structure $structureId..." }
        return safeApiCall(logger = LH.logger, errorContext = "Error fetching findings for structure $structureId.") {
            httpClient.get("/structures/$structureId/findings").body<List<FindingApiDto>>().map {
                it.toModel()
            }
        }.also { if (it is OnlineDataResult.Success) LH.logger.d { "Fetched ${it.data.size} findings for structure $structureId." } }
    }

    override suspend fun deleteFinding(id: Uuid): OnlineDataResult<Unit> {
        LH.logger.d { "Deleting finding $id from API..." }
        return safeApiCall(logger = LH.logger, errorContext = "Error deleting finding $id from API.") {
            httpClient.delete("/findings/$id")
            Unit
        }.also { if (it is OnlineDataResult.Success) LH.logger.d { "Deleted finding $id from API." } }
    }

    override suspend fun saveFinding(finding: FrontendFinding): OnlineDataResult<FrontendFinding?> {
        LH.logger.d { "Saving finding ${finding.finding.id} to API..." }
        return safeApiCall(logger = LH.logger, errorContext = "Error saving finding ${finding.finding.id} to API.") {
            val findingDto = finding.toPutApiDto()
            val response =
                httpClient.put("/structures/${finding.finding.structureId}/findings/${finding.finding.id}") {
                    setBody(findingDto)
                }
            if (response.status != HttpStatusCode.NoContent) {
                response.body<FindingApiDto>().toModel()
            } else {
                null
            }
        }.also { if (it is OnlineDataResult.Success) LH.logger.d { "Saved finding ${finding.finding.id} to API." } }
    }

    override suspend fun getFindingsModifiedSince(structureId: Uuid, since: Timestamp): OnlineDataResult<List<FindingSyncResult>> {
        LH.logger.d { "Fetching findings modified since $since for structure $structureId..." }
        return safeApiCall(logger = LH.logger, errorContext = "Error fetching findings modified since $since for structure $structureId.") {
            httpClient.get("/structures/$structureId/findings?since=${since.value}").body<List<FindingApiDto>>().map { dto ->
                if (dto.deletedAt != null) {
                    FindingSyncResult.Deleted(id = dto.id)
                } else {
                    FindingSyncResult.Updated(finding = dto.toModel())
                }
            }
        }.also { if (it is OnlineDataResult.Success) LH.logger.d { "Fetched ${it.data.size} modified findings for structure $structureId." } }
    }
}
