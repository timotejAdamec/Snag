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
import cz.adamec.timotej.snag.network.fe.NetworkException
import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.projects.fe.driven.internal.LH.logger
import cz.adamec.timotej.snag.projects.fe.driven.internal.api.ProjectsApi
import cz.adamec.timotej.snag.projects.fe.driven.internal.api.toApiDto
import cz.adamec.timotej.snag.projects.fe.driven.internal.api.toBusiness
import cz.adamec.timotej.snag.projects.fe.driven.internal.db.ProjectsDb
import cz.adamec.timotej.snag.projects.fe.driven.internal.db.toBusiness
import cz.adamec.timotej.snag.projects.fe.driven.internal.db.toEntity
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

internal class StoreProjectsRepository(
    private val projectsApi: ProjectsApi,
    private val projectsDb: ProjectsDb,
) : ProjectsRepository {
    override fun getAllProjectsFlow(): Flow<DataResult<List<Project>>> =
        channelFlow {
            send(DataResult.Loading)

            launch {
                projectsDb.getAllProjectsFlow().collectLatest { entities ->
                    send(DataResult.Success(entities.map { it.toBusiness() }))
                }
            }

            launch {
                try {
                    val remoteDtos = projectsApi.getProjects()
                    projectsDb.saveProjects(remoteDtos.map { it.toBusiness().toEntity() })
                } catch (e: Exception) {
                    logger.e(e) { "Error fetching projects from API" }
                }
            }
        }.onEach {
            logger.log(
                dataResult = it,
                additionalInfo = "getAllProjectsFlow",
            )
        }.distinctUntilChanged()

    override fun getProjectFlow(id: Uuid): Flow<DataResult<Project>> =
        channelFlow {
            send(DataResult.Loading)

            launch {
                projectsDb.getProjectFlow(id).collectLatest { entity ->
                    if (entity != null) {
                        send(DataResult.Success(entity.toBusiness()))
                    }
                }
            }

            launch {
                try {
                    val remoteDto = projectsApi.getProject(id)
                    projectsDb.saveProject(remoteDto.toBusiness().toEntity())
                } catch (e: Exception) {
                    logger.e(e) { "Error fetching project $id from API" }
                }
            }
        }.onEach {
            logger.log(
                dataResult = it,
                additionalInfo = "getProjectFlow, id $id",
            )
        }.distinctUntilChanged()

    override suspend fun saveProject(project: Project): DataResult<Project> {
        projectsDb.saveProject(project.toEntity())

        val result =
            try {
                val updatedDto = projectsApi.updateProject(project.toApiDto())
                val updatedProject = updatedDto.toBusiness()
                projectsDb.saveProject(updatedProject.toEntity())
                DataResult.Success(updatedProject)
            } catch (e: Exception) {
                logger.e(e) { "Error saving project $project to API" }
                if (e is NetworkException) {
                    DataResult.Success(project, locallyOnly = true)
                } else {
                    DataResult.Failure.ProgrammerError(e)
                }
            }

        logger.log(
            dataResult = result,
            additionalInfo = "saveProject, id ${project.id}",
        )

        return result
    }

    override suspend fun deleteProject(projectId: Uuid): DataResult<Unit> {
        projectsDb.deleteProject(projectId)

        val result =
            try {
                projectsApi.deleteProject(projectId)
                DataResult.Success(Unit)
            } catch (e: Exception) {
                logger.e(e) { "Error deleting project $projectId from API" }
                if (e is NetworkException) {
                    DataResult.Success(Unit, locallyOnly = true)
                } else {
                    DataResult.Failure.ProgrammerError(e)
                }
            }

        logger.log(
            dataResult = result,
            additionalInfo = "deleteProject, id $projectId",
        )

        return result
    }
}