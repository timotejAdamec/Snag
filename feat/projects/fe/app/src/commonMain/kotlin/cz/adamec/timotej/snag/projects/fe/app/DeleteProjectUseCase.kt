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
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.lib.core.fe.log
import cz.adamec.timotej.snag.projects.fe.app.internal.LH.logger
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsApi
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsDb
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

class DeleteProjectUseCase(
    private val projectsApi: ProjectsApi,
    private val projectsDb: ProjectsDb,
    private val applicationScope: ApplicationScope,
) {
    suspend operator fun invoke(projectId: Uuid): OfflineFirstDataResult<Unit> {
        applicationScope.launch {
            when (projectsApi.deleteProject(projectId)) {
                is OnlineDataResult.Failure -> logger.w {
                    "Error deleting project $projectId from API."
                }

                is OnlineDataResult.Success -> {
                    logger.d { "Deleted project $projectId from API." }
                }
            }
        }

        return projectsDb.deleteProject(projectId)
            .also {
                logger.log(
                    offlineFirstDataResult = it,
                    additionalInfo = "deleteProject, projectsDb.deleteProject($projectId)",
                )
            }
    }
}
