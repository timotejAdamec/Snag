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
import cz.adamec.timotej.snag.projects.fe.app.internal.LH
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsApi
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsDb
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class GetProjectsUseCase(
    private val projectsApi: ProjectsApi,
    private val projectsDb: ProjectsDb,
    private val applicationScope: ApplicationScope,
) {
    operator fun invoke(): Flow<OfflineFirstDataResult<List<Project>>> {
        applicationScope.launch {
            when (val remoteProjectsResult = projectsApi.getProjects()) {
                is OnlineDataResult.Failure ->
                    LH.logger.w(
                        "Error fetching projects, not updating local DB.",
                    )
                is OnlineDataResult.Success -> {
                    LH.logger.d {
                        "Fetched ${remoteProjectsResult.data.size} projects from API." +
                            " Saving them to local DB."
                    }
                    projectsDb.saveProjects(remoteProjectsResult.data)
                }
            }
        }

        return projectsDb
            .getAllProjectsFlow()
            .onEach {
                LH.logger.log(
                    offlineFirstDataResult = it,
                    additionalInfo = "GetProjectsUseCase, projectsDb.getAllProjectsFlow()",
                )
            }.distinctUntilChanged()
    }
}
