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

import cz.adamec.timotej.snag.projects.be.app.api.AssignUserToProjectUseCase
import cz.adamec.timotej.snag.projects.be.app.api.model.AssignUserToProjectRequest
import cz.adamec.timotej.snag.projects.be.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.projects.be.ports.ProjectAssignmentsDb

internal class AssignUserToProjectUseCaseImpl(
    private val projectAssignmentsDb: ProjectAssignmentsDb,
) : AssignUserToProjectUseCase {
    override suspend operator fun invoke(request: AssignUserToProjectRequest) {
        logger.debug("Assigning user {} to project {}.", request.userId, request.projectId)
        projectAssignmentsDb.assignUser(request.userId, request.projectId)
        logger.debug("Assigned user {} to project {}.", request.userId, request.projectId)
    }
}
