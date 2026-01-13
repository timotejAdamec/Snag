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

package cz.adamec.timotej.snag.projects.fe.driven.internal

import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.projects.fe.ports.ProjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse
import kotlin.uuid.Uuid

class StoreProjectsRepository(
    private val projectStore: ProjectStore,
) : ProjectRepository {
    override suspend fun getAllProjectsFlow(): List<Project> {
        TODO("Not yet implemented")
    }

    override suspend fun getProjectFlow(id: Uuid): Flow<Project?> =
        projectStore.stream(
            request = StoreReadRequest.cached(
                key = id,
                refresh = false,
            )
        ).map { response ->
            when (response) {
                is StoreReadResponse.Data -> response.value
                is StoreReadResponse.Error.Custom<*> -> TODO()
                is StoreReadResponse.Error.Exception -> TODO()
                is StoreReadResponse.Error.Message -> TODO()
                StoreReadResponse.Initial -> TODO()
                is StoreReadResponse.Loading -> TODO()
                is StoreReadResponse.NoNewData -> TODO()
            }
        }

    override suspend fun saveProject(project: Project) {
        TODO("Not yet implemented")
    }
}
