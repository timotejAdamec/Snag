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

package cz.adamec.timotej.snag.projects.fe.app

import cz.adamec.timotej.snag.lib.core.common.ApplicationScope
import cz.adamec.timotej.snag.lib.core.common.UuidProvider
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.lib.core.fe.log
import cz.adamec.timotej.snag.lib.core.fe.map
import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.projects.fe.app.internal.LH.logger
import cz.adamec.timotej.snag.projects.fe.app.model.SaveProjectRequest
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsApi
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsDb
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

class SaveProjectUseCase(
    private val projectsApi: ProjectsApi,
    private val projectsDb: ProjectsDb,
    private val applicationScope: ApplicationScope,
    private val uuidProvider: UuidProvider,
) {
    suspend operator fun invoke(request: SaveProjectRequest): OfflineFirstDataResult<Uuid> {
        val project =
            Project(
                id = request.id ?: uuidProvider.getUuid(),
                name = request.name,
                address = request.address,
            )

        applicationScope.launch {
            when (val remoteProjectResult = projectsApi.saveProject(project)) {
                is OnlineDataResult.Failure ->
                    logger.w {
                        "Error saving $project to API, not updating local DB."
                    }
                is OnlineDataResult.Success -> {
                    val updatedDto = remoteProjectResult.data
                    logger.d { "Saved $project to API." }
                    updatedDto?.let {
                        logger.d { "Saving fresher $updatedDto project from API to DB." }
                        projectsDb.saveProject(updatedDto)
                    } ?: logger.d { "No fresher project received from API." }
                }
            }
        }

        return projectsDb
            .saveProject(project)
            .also {
                logger.log(
                    offlineFirstDataResult = it,
                    additionalInfo = "SaveProjectUseCase, projectsDb.saveProject($project)",
                )
            }.map {
                project.id
            }
    }
}
