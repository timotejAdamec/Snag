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

import cz.adamec.timotej.snag.projects.be.app.api.CanCloseProjectUseCase
import cz.adamec.timotej.snag.projects.be.ports.ProjectsDb
import cz.adamec.timotej.snag.projects.business.CanCloseProjectRule
import cz.adamec.timotej.snag.users.be.app.api.GetUserUseCase
import kotlin.uuid.Uuid

internal class CanCloseProjectUseCaseImpl(
    private val getUserUseCase: GetUserUseCase,
    private val projectsDb: ProjectsDb,
    private val canCloseProjectRule: CanCloseProjectRule,
) : CanCloseProjectUseCase {
    override suspend operator fun invoke(
        userId: Uuid,
        projectId: Uuid,
    ): Boolean {
        val user = getUserUseCase(userId)
        val project = user?.let { projectsDb.getProject(projectId) }
        return if (user != null && project != null) {
            canCloseProjectRule(user = user, project = project)
        } else {
            false
        }
    }
}
