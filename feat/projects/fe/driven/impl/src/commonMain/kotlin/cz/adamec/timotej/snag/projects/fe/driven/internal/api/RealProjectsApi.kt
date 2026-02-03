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

import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.network.fe.SnagNetworkHttpClient
import cz.adamec.timotej.snag.network.fe.safeApiCall
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
    override suspend fun getProjects(): OnlineDataResult<List<Project>> {
        LH.logger.d { "Fetching projects..." }
        return safeApiCall(logger = LH.logger, errorContext = "Error fetching projects.") {
            httpClient.get("/projects").body<List<ProjectApiDto>>().map {
                it.toBusiness()
            }
        }.also { if (it is OnlineDataResult.Success) LH.logger.d { "Fetched ${it.data.size} projects." } }
    }

    override suspend fun getProject(id: Uuid): OnlineDataResult<Project> {
        LH.logger.d { "Fetching project $id..." }
        return safeApiCall(logger = LH.logger, errorContext = "Error fetching project $id.") {
            httpClient.get("/projects/$id").body<ProjectApiDto>().toBusiness()
        }.also { if (it is OnlineDataResult.Success) LH.logger.d { "Fetched project $id." } }
    }

    override suspend fun saveProject(project: Project): OnlineDataResult<Project?> {
        LH.logger.d { "Saving project ${project.id} to API..." }
        return safeApiCall(logger = LH.logger, errorContext = "Error saving project ${project.id} to API.") {
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
        }.also { if (it is OnlineDataResult.Success) LH.logger.d { "Saved project ${project.id} to API." } }
    }

    override suspend fun deleteProject(id: Uuid): OnlineDataResult<Unit> {
        LH.logger.d { "Deleting project $id from API..." }
        return safeApiCall(logger = LH.logger, errorContext = "Error deleting project $id from API.") {
            httpClient.delete("/projects/$id")
            Unit
        }.also { if (it is OnlineDataResult.Success) LH.logger.d { "Deleted project $id from API." } }
    }
}
