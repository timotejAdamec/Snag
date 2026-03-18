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

import cz.adamec.timotej.snag.projects.be.app.api.RemoveUserFromProjectUseCase
import cz.adamec.timotej.snag.projects.be.app.api.model.RemoveUserFromProjectRequest
import cz.adamec.timotej.snag.projects.be.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.projects.be.ports.ProjectAssignmentsDb

internal class RemoveUserFromProjectUseCaseImpl(
    private val projectAssignmentsDb: ProjectAssignmentsDb,
) : RemoveUserFromProjectUseCase {
    override suspend operator fun invoke(request: RemoveUserFromProjectRequest) {
        logger.debug("Removing user {} from project {}.", request.userId, request.projectId)
        projectAssignmentsDb.removeUser(request.userId, request.projectId)
        logger.debug("Removed user {} from project {}.", request.userId, request.projectId)
    }
}
