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

import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.network.fe.SnagNetworkHttpClient
import cz.adamec.timotej.snag.network.fe.safeApiCall
import cz.adamec.timotej.snag.projects.be.driving.contract.DeleteProjectApiDto
import cz.adamec.timotej.snag.projects.be.driving.contract.ProjectApiDto
import cz.adamec.timotej.snag.projects.fe.driven.internal.LH
import cz.adamec.timotej.snag.projects.fe.model.FrontendProject
import cz.adamec.timotej.snag.projects.fe.ports.ProjectSyncResult
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsApi
import io.ktor.client.call.body
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import kotlin.uuid.Uuid

internal class RealProjectsApi(
    private val httpClient: SnagNetworkHttpClient,
) : ProjectsApi {
    override suspend fun getProjects(): OnlineDataResult<List<FrontendProject>> {
        LH.logger.d { "Fetching projects..." }
        return safeApiCall(logger = LH.logger, errorContext = "Error fetching projects.") {
            httpClient.get("/projects").body<List<ProjectApiDto>>().map {
                it.toModel()
            }
        }.also { if (it is OnlineDataResult.Success) LH.logger.d { "Fetched ${it.data.size} projects." } }
    }

    override suspend fun getProject(id: Uuid): OnlineDataResult<FrontendProject> {
        LH.logger.d { "Fetching project $id..." }
        return safeApiCall(logger = LH.logger, errorContext = "Error fetching project $id.") {
            httpClient.get("/projects/$id").body<ProjectApiDto>().toModel()
        }.also { if (it is OnlineDataResult.Success) LH.logger.d { "Fetched project $id." } }
    }

    override suspend fun saveProject(project: FrontendProject): OnlineDataResult<FrontendProject?> {
        LH.logger.d { "Saving project ${project.project.id} to API..." }
        return safeApiCall(logger = LH.logger, errorContext = "Error saving project ${project.project.id} to API.") {
            val projectDto = project.toPutApiDto()
            val response =
                httpClient.put("/projects/${project.project.id}") {
                    setBody(projectDto)
                }
            if (response.status != HttpStatusCode.NoContent) {
                response.body<ProjectApiDto>().toModel()
            } else {
                null
            }
        }.also { if (it is OnlineDataResult.Success) LH.logger.d { "Saved project ${project.project.id} to API." } }
    }

    override suspend fun deleteProject(
        id: Uuid,
        deletedAt: Timestamp,
    ): OnlineDataResult<Unit> {
        LH.logger.d { "Deleting project $id from API..." }
        return safeApiCall(logger = LH.logger, errorContext = "Error deleting project $id from API.") {
            httpClient.delete("/projects/$id") {
                setBody(DeleteProjectApiDto(deletedAt = deletedAt))
            }
            Unit
        }.also { if (it is OnlineDataResult.Success) LH.logger.d { "Deleted project $id from API." } }
    }

    override suspend fun getProjectsModifiedSince(since: Timestamp): OnlineDataResult<List<ProjectSyncResult>> {
        LH.logger.d { "Fetching projects modified since $since..." }
        return safeApiCall(logger = LH.logger, errorContext = "Error fetching projects modified since $since.") {
            httpClient.get("/projects?since=${since.value}").body<List<ProjectApiDto>>().map { dto ->
                if (dto.deletedAt != null) {
                    ProjectSyncResult.Deleted(id = dto.id)
                } else {
                    ProjectSyncResult.Updated(project = dto.toModel())
                }
            }
        }.also { if (it is OnlineDataResult.Success) LH.logger.d { "Fetched ${it.data.size} modified projects." } }
    }
}
