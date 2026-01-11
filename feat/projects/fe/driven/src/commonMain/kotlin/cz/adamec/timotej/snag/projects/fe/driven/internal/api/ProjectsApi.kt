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

import cz.adamec.timotej.snag.network.fe.CallNetworkWithResult
import cz.adamec.timotej.snag.network.fe.NetworkResult
import cz.adamec.timotej.snag.network.fe.SnagNetworkHttpClient
import cz.adamec.timotej.snag.projects.be.driving.contract.ProjectApiDto
import io.ktor.client.call.body
import io.ktor.client.request.setBody
import kotlin.uuid.Uuid

internal class ProjectsApi(
    private val httpClient: SnagNetworkHttpClient,
    private val callNetworkWithResult: CallNetworkWithResult,
) {
    suspend fun getProjects(): NetworkResult<List<ProjectApiDto>> {
        return httpClient.get("/projects").body()
    }

    suspend fun getProject(id: Uuid): NetworkResult<ProjectApiDto> {
        return httpClient.get("/projects/$id").body()
    }

    suspend fun updateProject(project: ProjectApiDto): NetworkResult<ProjectApiDto> {
        val adwad: Instant
    }
}
