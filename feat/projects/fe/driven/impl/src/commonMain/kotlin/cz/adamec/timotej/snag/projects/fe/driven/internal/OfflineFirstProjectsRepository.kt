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

import cz.adamec.timotej.snag.lib.core.common.ApplicationScope
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.core.fe.log
import cz.adamec.timotej.snag.network.fe.NetworkException
import cz.adamec.timotej.snag.network.fe.log
import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.projects.fe.driven.internal.LH.logger
import cz.adamec.timotej.snag.projects.fe.driven.internal.api.ProjectsApi
import cz.adamec.timotej.snag.projects.fe.driven.internal.db.RealProjectsDb
import io.ktor.utils.io.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

internal class OfflineFirstProjectsRepository(
    private val projectsApi: ProjectsApi,
    private val projectsDb: RealProjectsDb,
    private val applicationScope: ApplicationScope,
) : ProjectsRepository {
    override fun getAllProjectsFlow(): Flow<OfflineFirstDataResult<List<Project>>> {

    }

    override fun getProjectFlow(id: Uuid): Flow<OfflineFirstDataResult<Project?>> {

    }

    override suspend fun saveProject(project: Project): OfflineFirstDataResult<Project> {
        applicationScope.launch {
            runCatching {
                val updatedProject = projectsApi.saveProject(project)
                updatedProject?.let {
                    projectsDb.saveProject(it)
                }
                return@runCatching updatedProject
            }.onSuccess { updatedDto ->
                logger.d { "Updated remote project ${project.id}." }
                updatedDto?.let {
                    logger.d { "Saved $updatedDto project from API." }
                } ?: logger.d { "No updated project received from API." }
            }.onFailure { e ->
                if (e is CancellationException) throw e
                if (e is NetworkException) e.log()
                else logger.e(e) { "Error saving project ${project.id} from API." }
            }
        }

        return try {
            projectsDb.saveProject(project)
            OfflineFirstDataResult.Success(project)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            OfflineFirstDataResult.ProgrammerError(e)
        }.also { result ->
            logger.log(
                offlineFirstDataResult = result,
                additionalInfo = "saveProject, id ${project.id}",
            )
        }
    }

    override suspend fun deleteProject(projectId: Uuid): OfflineFirstDataResult<Unit> {
        applicationScope.launch {
            runCatching {
                projectsApi.deleteProject(projectId)
            }.onSuccess {
                logger.d { "Deleted project $projectId from API." }
            }.onFailure { e ->
                if (e is CancellationException) throw e
                if (e is NetworkException) e.log()
                else logger.e(e) { "Error deleting project $projectId from API." }
            }
        }

        return try {
            projectsDb.deleteProject(projectId)
            OfflineFirstDataResult.Success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            return OfflineFirstDataResult.ProgrammerError(e)
        }.also { result ->
            logger.log(
                offlineFirstDataResult = result,
                additionalInfo = "deleteProject, id $projectId",
            )
        }
    }
}