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
import cz.adamec.timotej.snag.lib.core.map
import cz.adamec.timotej.snag.lib.store.StoreMutation
import cz.adamec.timotej.snag.lib.store.toDataResult
import cz.adamec.timotej.snag.lib.store.toDataResultFlow
import cz.adamec.timotej.snag.lib.store.toOfflineFirstDataResult
import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.projects.fe.driven.internal.LH.logger
import cz.adamec.timotej.snag.projects.fe.driven.internal.db.toBusiness
import cz.adamec.timotej.snag.projects.fe.driven.internal.db.toEntity
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreWriteRequest
import kotlin.uuid.Uuid

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
            .stream<ProjectMutation>(
                request =
                    StoreReadRequest.cached(
                        key = id,
                        refresh = true,
                    ),
            )
            .toDataResultFlow()
            .map { dataResult ->
                dataResult.map { mutation ->
                    (mutation as? StoreMutation.Save)?.value
                        ?: error("Unexpected mutation type in getProjectFlow: $mutation")
                }
            }
            .onEach {
                logger.log(
                    dataResult = it,
                    additionalInfo = "getProjectFlow, id $id",
                )
            }.distinctUntilChanged()

    override suspend fun saveProject(project: Project): DataResult<Project> {
        val result: DataResult<Project> = projectStore.write(
            StoreWriteRequest.of(
                key = project.id,
                value = StoreMutation.Save(project),
            ),
        )
            .toOfflineFirstDataResult<LocalProjectMutation>(StoreMutation.Save(project.toEntity()))
            .map { mutation: LocalProjectMutation ->
                (mutation as? StoreMutation.Save)?.value?.toBusiness()
                    ?: error("Unexpected mutation type in saveProject: $mutation")
            }

        logger.log(
            dataResult = result,
            additionalInfo = "saveProject, id ${project.id}",
        )

        return result
    }

    override suspend fun deleteProject(projectId: Uuid): DataResult<Unit> {
        val result: DataResult<Unit> = projectStore.write(
            StoreWriteRequest.of(
                key = projectId,
                value = StoreMutation.Delete(projectId)
            )
        ).toOfflineFirstDataResult(Unit)

        logger.log(
            dataResult = result,
            additionalInfo = "deleteProject, id $projectId",
        )

        return result
    }
}
