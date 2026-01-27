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
import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.projects.fe.app.internal.LH.logger
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsApi
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsDb
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

class GetProjectUseCase(
    private val projectsApi: ProjectsApi,
    private val projectsDb: ProjectsDb,
    private val applicationScope: ApplicationScope,
) {
    operator fun invoke(projectId: Uuid): Flow<OfflineFirstDataResult<Project?>> {
        applicationScope.launch {
            when (val remoteProjectResult = projectsApi.getProject(projectId)) {
                is OnlineDataResult.Failure ->
                    logger.w(
                        "Error fetching project $projectId, not updating local DB.",
                    )
                is OnlineDataResult.Success -> {
                    logger.d {
                        "Fetched project $projectId from API." +
                            " Saving it to local DB."
                    }
                    projectsDb.saveProject(remoteProjectResult.data)
                }
            }
        }

        return projectsDb
            .getProjectFlow(projectId)
            .onEach {
                logger.log(
                    offlineFirstDataResult = it,
                    additionalInfo = "GetProjectUseCase, projectsDb.getProjectFlow($projectId)",
                )
            }.distinctUntilChanged()
    }
}
