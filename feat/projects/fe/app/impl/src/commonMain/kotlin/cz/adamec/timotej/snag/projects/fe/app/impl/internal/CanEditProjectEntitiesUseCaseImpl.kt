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

import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.projects.business.AreProjectEntitiesEditableRule
import cz.adamec.timotej.snag.projects.business.CanAccessProjectRule
import cz.adamec.timotej.snag.projects.fe.app.api.CanEditProjectEntitiesUseCase
import cz.adamec.timotej.snag.projects.fe.ports.ProjectAssignmentsDb
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsDb
import cz.adamec.timotej.snag.users.fe.app.api.GetCurrentUserFlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.uuid.Uuid

class CanEditProjectEntitiesUseCaseImpl(
    private val projectsDb: ProjectsDb,
    private val projectAssignmentsDb: ProjectAssignmentsDb,
    private val getCurrentUserFlowUseCase: GetCurrentUserFlowUseCase,
    private val canAccessProjectRule: CanAccessProjectRule,
    private val areProjectEntitiesEditableRule: AreProjectEntitiesEditableRule,
) : CanEditProjectEntitiesUseCase {
    override operator fun invoke(projectId: Uuid): Flow<Boolean> =
        combine(
            getCurrentUserFlowUseCase(),
            projectsDb.getProjectFlow(projectId),
            projectAssignmentsDb.getAssignedUserIdsFlow(projectId),
        ) { userResult, projectResult, assignmentsResult ->
            val user = (userResult as? OfflineFirstDataResult.Success)?.data
            val project = (projectResult as? OfflineFirstDataResult.Success)?.data
            val assignedUserIds = (assignmentsResult as? OfflineFirstDataResult.Success)?.data
            if (user != null && project != null && assignedUserIds != null) {
                canAccessProjectRule(
                    user = user,
                    project = project,
                    assignedUserIds = assignedUserIds,
                ) &&
                    areProjectEntitiesEditableRule(project)
            } else {
                false
            }
        }.catch { emit(false) }
            .distinctUntilChanged()
}
