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

package cz.adamec.timotej.snag.projects.fe.app.impl.internal

import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.projects.fe.app.api.CascadeRestoreLocalAssignmentsByProjectIdUseCase
import cz.adamec.timotej.snag.projects.fe.ports.ProjectAssignmentsDb
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsApi
import kotlin.uuid.Uuid

internal class CascadeRestoreLocalAssignmentsByProjectIdUseCaseImpl(
    private val projectsApi: ProjectsApi,
    private val projectAssignmentsDb: ProjectAssignmentsDb,
) : CascadeRestoreLocalAssignmentsByProjectIdUseCase {
    override suspend operator fun invoke(projectId: Uuid) {
        when (val result = projectsApi.getProjectAssignments(projectId)) {
            is OnlineDataResult.Success -> {
                projectAssignmentsDb.replaceAssignments(
                    projectId = projectId,
                    userIds = result.data,
                )
            }
            is OnlineDataResult.Failure -> {
                LH.logger.w { "Failed to restore assignments for project $projectId." }
            }
        }
    }
}
