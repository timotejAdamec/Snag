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

package cz.adamec.timotej.snag.projects.fe.app.impl.internal.sync

import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.LH
import cz.adamec.timotej.snag.projects.fe.ports.ProjectAssignmentsDb
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsApi
import cz.adamec.timotej.snag.sync.fe.app.api.handler.PullSyncOperationHandler
import cz.adamec.timotej.snag.sync.fe.app.api.handler.PullSyncOperationResult
import kotlin.uuid.Uuid

internal class ProjectAssignmentsPullSyncHandler(
    private val projectsApi: ProjectsApi,
    private val projectAssignmentsDb: ProjectAssignmentsDb,
) : PullSyncOperationHandler {
    override val entityTypeId: String = PROJECT_ASSIGNMENT_SYNC_ENTITY_TYPE

    override suspend fun execute(scopeId: Uuid?): PullSyncOperationResult {
        val projectId = requireNotNull(scopeId) { "scopeId (projectId) is required for assignment sync." }
        LH.logger.d { "Pulling assignments for project $projectId..." }

        return when (val result = projectsApi.getProjectAssignments(projectId)) {
            is OnlineDataResult.Failure -> {
                LH.logger.w { "Error pulling assignments for project $projectId." }
                PullSyncOperationResult.Failure
            }
            is OnlineDataResult.Success -> {
                projectAssignmentsDb.replaceAssignments(
                    projectId = projectId,
                    userIds = result.data,
                )
                LH.logger.d { "Pulled ${result.data.size} assignment(s) for project $projectId." }
                PullSyncOperationResult.Success
            }
        }
    }
}
