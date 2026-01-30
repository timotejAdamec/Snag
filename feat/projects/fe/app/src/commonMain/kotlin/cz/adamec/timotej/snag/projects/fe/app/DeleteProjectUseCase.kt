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

import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.core.fe.log
import cz.adamec.timotej.snag.projects.fe.app.internal.LH.logger
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsDb
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsSync
import kotlin.uuid.Uuid

class DeleteProjectUseCase(
    private val projectsDb: ProjectsDb,
    private val projectsSync: ProjectsSync,
) {
    suspend operator fun invoke(projectId: Uuid): OfflineFirstDataResult<Unit> =
        projectsDb
            .deleteProject(projectId)
            .also {
                logger.log(
                    offlineFirstDataResult = it,
                    additionalInfo = "deleteProject, projectsDb.deleteProject($projectId)",
                )
                if (it is OfflineFirstDataResult.Success) {
                    projectsSync.enqueueProjectDelete(projectId)
                }
            }
}
