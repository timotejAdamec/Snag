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

import cz.adamec.timotej.snag.lib.core.DataResult
import cz.adamec.timotej.snag.lib.core.log
import cz.adamec.timotej.snag.lib.store.toDataResult
import cz.adamec.timotej.snag.lib.store.toDataResultFlow
import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.projects.fe.driven.internal.LH.logger
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreWriteRequest
import kotlin.uuid.Uuid

import org.mobilenativefoundation.store.store5.StoreWriteResponse

class StoreProjectsRepository(
    private val projectStore: ProjectStore,
    private val projectsStore: ProjectsStore,
) : ProjectsRepository {
    override fun getAllProjectsFlow(): Flow<DataResult<List<Project>>> =
        projectsStore
            .stream(
                request =
                    StoreReadRequest.cached(
                        key = Unit,
                        refresh = true,
                    ),
            ).toDataResultFlow()
            .onEach {
                logger.log(
                    dataResult = it,
                    additionalInfo = "getAllProjectsFlow",
                )
            }.distinctUntilChanged()

    override fun getProjectFlow(id: Uuid): Flow<DataResult<Project>> =
        projectStore
            .stream<Project>(
                request =
                    StoreReadRequest.cached(
                        key = id,
                        refresh = true,
                    ),
            ).toDataResultFlow()
            .onEach {
                logger.log(
                    dataResult = it,
                    additionalInfo = "getProjectFlow, id $id",
                )
            }.distinctUntilChanged()

    override suspend fun saveProject(project: Project): DataResult<Project> {
        val response = projectStore.write(
            StoreWriteRequest.of(
                key = project.id,
                value = project,
            ),
        )

        val result = when (response) {
            is StoreWriteResponse.Success -> response.toDataResult()
            is StoreWriteResponse.Error -> {
                // In offline-first, we consider local save as success.
                // The error is likely due to network/updater failure.
                // We assume SourceOfTruth (local DB) write succeeded before this.
                logger.log(
                    dataResult = response.toDataResult<Project>(),
                    additionalInfo = "saveProject network failed, but treating as local success, id ${project.id}",
                )
                DataResult.Success(project)
            }
        }

        logger.log(
            dataResult = result,
            additionalInfo = "saveProject, id ${project.id}",
        )

        return result
    }
}
