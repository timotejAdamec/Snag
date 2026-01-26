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

import cz.adamec.timotej.snag.network.fe.SnagNetworkHttpClient
import cz.adamec.timotej.snag.projects.be.driving.contract.ProjectApiDto
import cz.adamec.timotej.snag.projects.business.Project
import io.ktor.client.call.body
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import kotlin.uuid.Uuid

internal class ProjectsApi(
    private val httpClient: SnagNetworkHttpClient,
) {
    suspend fun getProjects(): List<Project> =
        httpClient.get("/projects").body<List<ProjectApiDto>>().map {
            it.toBusiness()
        }

    suspend fun getProject(id: Uuid): Project =
        httpClient.get("/projects/$id").body<ProjectApiDto>().toBusiness()

    suspend fun saveProject(project: Project): Project? {
        val projectDto = project.toApiDto()
        val response = httpClient.put("/projects/${projectDto.id}") {
            setBody(projectDto)
        }
        return if (response.status != HttpStatusCode.NoContent) {
            response.body<ProjectApiDto>().toBusiness()
        } else null
    }

    suspend fun deleteProject(id: Uuid) {
        httpClient.delete("/projects/$id")
    }
}
