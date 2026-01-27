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
import cz.adamec.timotej.snag.lib.core.common.runCatchingCancellable
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.network.fe.NetworkException
import cz.adamec.timotej.snag.network.fe.SnagNetworkHttpClient
import cz.adamec.timotej.snag.network.fe.log
import cz.adamec.timotej.snag.network.fe.toOnlineDataResult
import cz.adamec.timotej.snag.structures.be.driving.contract.StructureApiDto
import cz.adamec.timotej.snag.structures.fe.driven.internal.LH
import cz.adamec.timotej.snag.structures.fe.ports.StructuresApi
import io.ktor.client.call.body
import kotlin.uuid.Uuid

internal class RealStructuresApi(
    private val httpClient: SnagNetworkHttpClient,
) : StructuresApi {
    override suspend fun getStructures(projectId: Uuid): OnlineDataResult<List<Structure>> =
        runCatchingCancellable {
            LH.logger.d { "Fetching structures for project $projectId..." }
            httpClient.get("/projects/$projectId/structures").body<List<StructureApiDto>>().map {
                it.toBusiness()
            }
        }.fold(
            onSuccess = {
                LH.logger.d { "Fetched ${it.size} structures for project $projectId." }
                OnlineDataResult.Success(it)
            },
            onFailure = { e ->
                return if (e is NetworkException) {
                    e.log()
                    e.toOnlineDataResult()
                } else {
                    LH.logger.e { "Error fetching structures for project $projectId." }
                    OnlineDataResult.Failure.ProgrammerError(
                        throwable = e,
                    )
                }
            },
        )
}
