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

package cz.adamec.timotej.snag.projects.fe.driven.internal.api

import cz.adamec.timotej.snag.lib.core.common.runCatchingCancellable
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.network.fe.NetworkException
import cz.adamec.timotej.snag.network.fe.SnagNetworkHttpClient
import cz.adamec.timotej.snag.network.fe.log
import cz.adamec.timotej.snag.network.fe.toOnlineDataResult
import cz.adamec.timotej.snag.projects.be.driving.contract.ProjectApiDto
import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.projects.fe.driven.internal.LH
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsApi
import io.ktor.client.call.body
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import kotlin.uuid.Uuid

internal class RealProjectsApi(
    private val httpClient: SnagNetworkHttpClient,
) : ProjectsApi {
    override suspend fun getProjects(): OnlineDataResult<List<Project>> =
        runCatchingCancellable {
            LH.logger.d { "Fetching projects..." }
            httpClient.get("/projects").body<List<ProjectApiDto>>().map {
                it.toBusiness()
            }
        }.fold(
            onSuccess = {
                LH.logger.d { "Fetched ${it.size} projects." }
                OnlineDataResult.Success(it)
            },
            onFailure = { e ->
                return if (e is NetworkException) {
                    e.log()
                    e.toOnlineDataResult()
                } else {
                    LH.logger.e { "Error fetching projects." }
                    OnlineDataResult.Failure.ProgrammerError(
                        throwable = e,
                    )
                }
            },
        )

    override suspend fun getProject(id: Uuid): OnlineDataResult<Project> =
        runCatchingCancellable {
            LH.logger.d { "Fetching project $id..." }
            httpClient.get("/projects/$id").body<ProjectApiDto>().toBusiness()
        }.fold(
            onSuccess = {
                LH.logger.d { "Fetched project $id." }
                OnlineDataResult.Success(it)
            },
            onFailure = { e ->
                return if (e is NetworkException) {
                    e.log()
                    e.toOnlineDataResult()
                } else {
                    LH.logger.e { "Error fetching project $id." }
                    OnlineDataResult.Failure.ProgrammerError(
                        throwable = e,
                    )
                }
            },
        )

    override suspend fun saveProject(project: Project): OnlineDataResult<Project?> =
        runCatchingCancellable {
            LH.logger.d { "Saving project ${project.id} to API..." }
            val projectDto = project.toPutApiDto()
            val response =
                httpClient.put("/projects/${project.id}") {
                    setBody(projectDto)
                }
            if (response.status != HttpStatusCode.NoContent) {
                response.body<ProjectApiDto>().toBusiness()
            } else {
                null
            }
        }.fold(
            onSuccess = {
                LH.logger.d { "Saved project ${project.id} to API." }
                OnlineDataResult.Success(it)
            },
            onFailure = { e ->
                return if (e is NetworkException) {
                    e.log()
                    e.toOnlineDataResult()
                } else {
                    LH.logger.e { "Error saving project ${project.id} to API." }
                    OnlineDataResult.Failure.ProgrammerError(
                        throwable = e,
                    )
                }
            },
        )

    override suspend fun deleteProject(id: Uuid): OnlineDataResult<Unit> =
        runCatchingCancellable {
            httpClient.delete("/projects/$id")
        }.fold(
            onSuccess = {
                LH.logger.d { "Deleted project $id from API." }
                OnlineDataResult.Success(Unit)
            },
            onFailure = { e ->
                return if (e is NetworkException) {
                    e.log()
                    e.toOnlineDataResult()
                } else {
                    LH.logger.e { "Error deleting project $id from API." }
                    OnlineDataResult.Failure.ProgrammerError(
                        throwable = e,
                    )
                }
            },
        )
}
