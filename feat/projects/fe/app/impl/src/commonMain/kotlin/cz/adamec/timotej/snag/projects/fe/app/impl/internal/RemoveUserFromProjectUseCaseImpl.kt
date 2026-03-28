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
import cz.adamec.timotej.snag.projects.fe.app.api.RemoveUserFromProjectUseCase
import cz.adamec.timotej.snag.projects.fe.ports.ProjectAssignmentsDb
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsApi
import kotlin.uuid.Uuid

internal class RemoveUserFromProjectUseCaseImpl(
    private val projectsApi: ProjectsApi,
    private val projectAssignmentsDb: ProjectAssignmentsDb,
) : RemoveUserFromProjectUseCase {
    override suspend operator fun invoke(
        projectId: Uuid,
        userId: Uuid,
    ) {
        LH.logger.d { "Removing user $userId from project $projectId..." }
        when (projectsApi.removeUserFromProject(projectId = projectId, userId = userId)) {
            is OnlineDataResult.Success -> {
                LH.logger.d { "Removed user $userId from project $projectId. Refreshing local assignments..." }
                when (val assignments = projectsApi.getProjectAssignments(projectId)) {
                    is OnlineDataResult.Success ->
                        projectAssignmentsDb.replaceAssignments(
                            projectId = projectId,
                            userIds = assignments.data,
                        )
                    is OnlineDataResult.Failure ->
                        LH.logger.w { "Failed to refresh assignments for project $projectId after remove." }
                }
            }
            is OnlineDataResult.Failure -> {
                LH.logger.w { "Failed to remove user $userId from project $projectId." }
            }
        }
    }
}
