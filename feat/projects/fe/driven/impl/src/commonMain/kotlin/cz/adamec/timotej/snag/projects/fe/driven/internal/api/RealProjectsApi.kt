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

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.core.network.fe.safeApiCall
import cz.adamec.timotej.snag.network.fe.SnagNetworkHttpClient
import cz.adamec.timotej.snag.projects.app.model.AppProject
import cz.adamec.timotej.snag.projects.be.driving.contract.DeleteProjectApiDto
import cz.adamec.timotej.snag.projects.be.driving.contract.ProjectApiDto
import cz.adamec.timotej.snag.projects.fe.driven.internal.LH
import cz.adamec.timotej.snag.projects.fe.ports.ProjectSyncResult
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsApi
import io.ktor.client.call.body
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

internal class RealProjectsApi(
    private val httpClient: SnagNetworkHttpClient,
) : ProjectsApi {
    override suspend fun getProjects(): OnlineDataResult<List<AppProject>> {
        LH.logger.d { "Fetching projects..." }
        return safeApiCall(logger = LH.logger, errorContext = "Error fetching projects.") {
            httpClient.get("/projects").body<List<ProjectApiDto>>().map {
                it.toModel()
            }
        }.also { if (it is OnlineDataResult.Success) LH.logger.d { "Fetched ${it.data.size} projects." } }
    }

    override suspend fun getProject(id: Uuid): OnlineDataResult<AppProject> {
        LH.logger.d { "Fetching project $id..." }
        return safeApiCall(logger = LH.logger, errorContext = "Error fetching project $id.") {
            httpClient.get("/projects/$id").body<ProjectApiDto>().toModel()
        }.also { if (it is OnlineDataResult.Success) LH.logger.d { "Fetched project $id." } }
    }

    override suspend fun saveProject(project: AppProject): OnlineDataResult<AppProject?> {
        LH.logger.d { "Saving project ${project.id} to API..." }
        return safeApiCall(logger = LH.logger, errorContext = "Error saving project ${project.id} to API.") {
            val projectDto = project.toPutApiDto()
            val response =
                httpClient.put("/projects/${project.id}") {
                    setBody(projectDto)
                }
            if (response.status != HttpStatusCode.NoContent) {
                response.body<ProjectApiDto>().toModel()
            } else {
                null
            }
        }.also { if (it is OnlineDataResult.Success) LH.logger.d { "Saved project ${project.id} to API." } }
    }

    override suspend fun deleteProject(
        id: Uuid,
        deletedAt: Timestamp,
    ): OnlineDataResult<AppProject?> {
        LH.logger.d { "Deleting project $id from API..." }
        return safeApiCall(logger = LH.logger, errorContext = "Error deleting project $id from API.") {
            val response =
                httpClient.patch("/projects/$id") {
                    setBody(DeleteProjectApiDto(deletedAt = deletedAt))
                }
            if (response.status != HttpStatusCode.NoContent) {
                response.body<ProjectApiDto>().toModel()
            } else {
                null
            }
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

    override suspend fun getProjectAssignments(projectId: Uuid): OnlineDataResult<Set<Uuid>> {
        LH.logger.d { "Fetching assignments for project $projectId..." }
        return safeApiCall(
            logger = LH.logger,
            errorContext = "Error fetching assignments for project $projectId.",
        ) {
            httpClient
                .get("/projects/$projectId/assignments")
                .body<List<AssignedUserApiDto>>()
                .map { Uuid.parse(it.id) }
                .toSet()
        }.also {
            if (it is OnlineDataResult.Success) {
                LH.logger.d { "Fetched ${it.data.size} assignments for project $projectId." }
            }
        }
    }

    override suspend fun assignUserToProject(
        projectId: Uuid,
        userId: Uuid,
    ): OnlineDataResult<Unit> {
        LH.logger.d { "Assigning user $userId to project $projectId..." }
        return safeApiCall(
            logger = LH.logger,
            errorContext = "Error assigning user $userId to project $projectId.",
        ) {
            httpClient.put("/projects/$projectId/assignments/$userId")
            Unit
        }.also {
            if (it is OnlineDataResult.Success) {
                LH.logger.d { "Assigned user $userId to project $projectId." }
            }
        }
    }

    override suspend fun removeUserFromProject(
        projectId: Uuid,
        userId: Uuid,
    ): OnlineDataResult<Unit> {
        LH.logger.d { "Removing user $userId from project $projectId..." }
        return safeApiCall(
            logger = LH.logger,
            errorContext = "Error removing user $userId from project $projectId.",
        ) {
            httpClient.delete("/projects/$projectId/assignments/$userId")
            Unit
        }.also {
            if (it is OnlineDataResult.Success) {
                LH.logger.d { "Removed user $userId from project $projectId." }
            }
        }
    }
}

@Serializable
private data class AssignedUserApiDto(
    val id: String,
)
