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

import cz.adamec.timotej.snag.core.foundation.common.ApplicationScope
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.core.network.fe.log
import cz.adamec.timotej.snag.projects.app.model.AppProject
import cz.adamec.timotej.snag.projects.business.CanAccessProjectRule
import cz.adamec.timotej.snag.projects.fe.app.api.GetProjectsUseCase
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.sync.PROJECT_SYNC_ENTITY_TYPE
import cz.adamec.timotej.snag.projects.fe.ports.ProjectAssignmentsDb
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsDb
import cz.adamec.timotej.snag.sync.fe.app.api.ExecutePullSyncUseCase
import cz.adamec.timotej.snag.sync.fe.app.api.model.ExecutePullSyncRequest
import cz.adamec.timotej.snag.users.app.model.AppUser
import cz.adamec.timotej.snag.users.fe.app.api.GetCurrentUserFlowUseCase
import cz.adamec.timotej.snag.users.fe.app.api.GetUsersUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class GetProjectsUseCaseImpl(
    private val executePullSyncUseCase: ExecutePullSyncUseCase,
    private val projectsDb: ProjectsDb,
    private val projectAssignmentsDb: ProjectAssignmentsDb,
    private val getCurrentUserFlowUseCase: GetCurrentUserFlowUseCase,
    private val getUsersUseCase: GetUsersUseCase,
    private val canAccessProjectRule: CanAccessProjectRule,
    private val applicationScope: ApplicationScope,
) : GetProjectsUseCase {
    override operator fun invoke(): Flow<OfflineFirstDataResult<List<AppProject>>> {
        applicationScope.launch {
            executePullSyncUseCase(ExecutePullSyncRequest(entityTypeId = PROJECT_SYNC_ENTITY_TYPE))
        }

        return combine(
            projectsDb.getAllProjectsFlow(),
            getCurrentUserFlowUseCase(),
            getUsersUseCase(),
        ) { projectsResult, userResult, usersResult ->
            filterByAccess(
                projectsResult = projectsResult,
                userResult = userResult,
                usersResult = usersResult,
            )
        }.onEach {
            LH.logger.log(
                offlineFirstDataResult = it,
                additionalInfo = "GetProjectsUseCase, filtered by access",
            )
        }.distinctUntilChanged()
    }

    private suspend fun filterByAccess(
        projectsResult: OfflineFirstDataResult<List<AppProject>>,
        userResult: OfflineFirstDataResult<AppUser?>,
        usersResult: OfflineFirstDataResult<List<AppUser>>,
    ): OfflineFirstDataResult<List<AppProject>> {
        val projects = (projectsResult as? OfflineFirstDataResult.Success)?.data
        val currentUser = (userResult as? OfflineFirstDataResult.Success)?.data
        val users = (usersResult as? OfflineFirstDataResult.Success)?.data

        if (projects == null || currentUser == null || users == null) {
            return OfflineFirstDataResult.Success(emptyList())
        }

        val usersById = users.associateBy { it.id }
        val assignedProjectIds =
            projectAssignmentsDb.getProjectIdsForAssignedUser(currentUser.id)

        return OfflineFirstDataResult.Success(
            projects.filter { project ->
                canAccessProjectRule(
                    user = currentUser,
                    project = project,
                    assignedUserIds =
                        if (project.id in assignedProjectIds) {
                            setOf(currentUser.id)
                        } else {
                            emptySet()
                        },
                    projectCreatorRole = usersById[project.creatorId]?.role,
                )
            },
        )
    }
}
