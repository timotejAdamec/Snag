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

package cz.adamec.timotej.snag.projects.be.app.impl.internal

import cz.adamec.timotej.snag.projects.be.app.api.CanAccessProjectUseCase
import cz.adamec.timotej.snag.projects.be.ports.ProjectAssignmentsDb
import cz.adamec.timotej.snag.projects.be.ports.ProjectsDb
import cz.adamec.timotej.snag.projects.business.CanAccessProjectRule
import cz.adamec.timotej.snag.users.be.app.api.GetUserUseCase
import kotlin.uuid.Uuid

internal class CanAccessProjectUseCaseImpl(
    private val getUserUseCase: GetUserUseCase,
    private val projectsDb: ProjectsDb,
    private val projectAssignmentsDb: ProjectAssignmentsDb,
    private val canAccessProjectRule: CanAccessProjectRule,
) : CanAccessProjectUseCase {
    override suspend operator fun invoke(
        userId: Uuid,
        projectId: Uuid,
    ): Boolean {
        val user = getUserUseCase(userId)
        val project = user?.let { projectsDb.getProject(projectId) }
        return if (user != null && project != null) {
            val assignedUsers = projectAssignmentsDb.getAssignedUsers(projectId)
            val assignedUserIds = assignedUsers.map { it.id }.toSet()
            val creator = getUserUseCase(project.creatorId)
            canAccessProjectRule(
                user = user,
                project = project,
                assignedUserIds = assignedUserIds,
                projectCreatorRole = creator?.role,
            )
        } else {
            false
        }
    }
}
