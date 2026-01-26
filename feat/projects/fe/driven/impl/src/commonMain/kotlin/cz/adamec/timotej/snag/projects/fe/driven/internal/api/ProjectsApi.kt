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
import io.ktor.client.call.body
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import kotlin.uuid.Uuid

internal class ProjectsApi(
    private val httpClient: SnagNetworkHttpClient,
) {
    suspend fun getProjects(): List<ProjectApiDto> = httpClient.get("/projects").body()

    suspend fun getProject(id: Uuid): ProjectApiDto = httpClient.get("/projects/$id").body()

    suspend fun saveProject(project: ProjectApiDto): ProjectApiDto? {
        val response = httpClient.put("/projects/${project.id}") {
            setBody(project)
        }
        return if (response.status != HttpStatusCode.NoContent) response.body() else null
    }

    suspend fun deleteProject(id: Uuid) {
        httpClient.delete("/projects/$id")
    }
}
