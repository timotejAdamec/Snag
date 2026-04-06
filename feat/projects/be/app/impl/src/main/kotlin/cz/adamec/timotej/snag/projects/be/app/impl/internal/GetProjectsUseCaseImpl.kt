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

import cz.adamec.timotej.snag.authorization.business.UserRole
import cz.adamec.timotej.snag.projects.be.app.api.GetProjectsUseCase
import cz.adamec.timotej.snag.projects.be.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.projects.be.model.BackendProject
import cz.adamec.timotej.snag.projects.be.ports.ProjectAssignmentsDb
import cz.adamec.timotej.snag.projects.be.ports.ProjectsDb
import cz.adamec.timotej.snag.users.be.app.api.GetUserUseCase
import kotlin.uuid.Uuid

internal class GetProjectsUseCaseImpl(
    private val projectsDb: ProjectsDb,
    private val getUserUseCase: GetUserUseCase,
    private val projectAssignmentsDb: ProjectAssignmentsDb,
) : GetProjectsUseCase {
    override suspend operator fun invoke(userId: Uuid): List<BackendProject> {
        logger.debug("Getting projects from local storage.")
        val user = getUserUseCase(userId) ?: return emptyList()
        val allProjects = projectsDb.getProjects()

        return filterProjectsByAccess(
            projects = allProjects,
            userId = userId,
            userRole = user.role,
        ).also {
            logger.debug("Got {} accessible projects from local storage.", it.size)
        }
    }

    private suspend fun filterProjectsByAccess(
        projects: List<BackendProject>,
        userId: Uuid,
        userRole: UserRole?,
    ): List<BackendProject> {
        if (userRole == UserRole.ADMINISTRATOR) return projects

        val assignedProjectIds =
            projectAssignmentsDb.getProjectsForUser(userId).toSet()

        return projects.filter { project ->
            project.creatorId == userId || project.id in assignedProjectIds
        }
    }
}
