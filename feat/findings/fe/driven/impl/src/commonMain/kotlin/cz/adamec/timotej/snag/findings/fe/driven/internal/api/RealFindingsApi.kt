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

import cz.adamec.timotej.snag.feat.findings.business.Finding
import cz.adamec.timotej.snag.findings.be.driving.contract.FindingApiDto
import cz.adamec.timotej.snag.findings.fe.driven.internal.LH
import cz.adamec.timotej.snag.findings.fe.ports.FindingsApi
import cz.adamec.timotej.snag.lib.core.common.runCatchingCancellable
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.network.fe.NetworkException
import cz.adamec.timotej.snag.network.fe.SnagNetworkHttpClient
import cz.adamec.timotej.snag.network.fe.log
import cz.adamec.timotej.snag.network.fe.toOnlineDataResult
import io.ktor.client.call.body
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import kotlin.uuid.Uuid

internal class RealFindingsApi(
    private val httpClient: SnagNetworkHttpClient,
) : FindingsApi {
    override suspend fun getFindings(structureId: Uuid): OnlineDataResult<List<Finding>> =
        runCatchingCancellable {
            LH.logger.d { "Fetching findings for structure $structureId..." }
            httpClient.get("/structures/$structureId/findings").body<List<FindingApiDto>>().map {
                it.toBusiness()
            }
        }.fold(
            onSuccess = {
                LH.logger.d { "Fetched ${it.size} findings for structure $structureId." }
                OnlineDataResult.Success(it)
            },
            onFailure = { e ->
                return if (e is NetworkException) {
                    e.log()
                    e.toOnlineDataResult()
                } else {
                    LH.logger.e { "Error fetching findings for structure $structureId." }
                    OnlineDataResult.Failure.ProgrammerError(
                        throwable = e,
                    )
                }
            },
        )

    override suspend fun deleteFinding(id: Uuid): OnlineDataResult<Unit> =
        runCatchingCancellable {
            httpClient.delete("/findings/$id")
        }.fold(
            onSuccess = {
                LH.logger.d { "Deleted finding $id from API." }
                OnlineDataResult.Success(Unit)
            },
            onFailure = { e ->
                return if (e is NetworkException) {
                    e.log()
                    e.toOnlineDataResult()
                } else {
                    LH.logger.e { "Error deleting finding $id from API." }
                    OnlineDataResult.Failure.ProgrammerError(
                        throwable = e,
                    )
                }
            },
        )

    override suspend fun saveFinding(finding: Finding): OnlineDataResult<Finding?> =
        runCatchingCancellable {
            LH.logger.d { "Saving finding ${finding.id} to API..." }
            val findingDto = finding.toPutApiDto()
            val response =
                httpClient.put("/structures/${finding.structureId}/findings/${finding.id}") {
                    setBody(findingDto)
                }
            if (response.status != HttpStatusCode.NoContent) {
                response.body<FindingApiDto>().toBusiness()
            } else {
                null
            }
        }.fold(
            onSuccess = {
                LH.logger.d { "Saved finding ${finding.id} to API." }
                OnlineDataResult.Success(it)
            },
            onFailure = { e ->
                return if (e is NetworkException) {
                    e.log()
                    e.toOnlineDataResult()
                } else {
                    LH.logger.e { "Error saving finding ${finding.id} to API." }
                    OnlineDataResult.Failure.ProgrammerError(
                        throwable = e,
                    )
                }
            },
        )
}
