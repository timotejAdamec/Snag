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

package cz.adamec.timotej.snag.users.be.app.impl.internal

import cz.adamec.timotej.snag.users.be.app.api.RemoveUserFromProjectUseCase
import cz.adamec.timotej.snag.users.be.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.users.be.ports.ProjectAssignmentsDb
import kotlin.uuid.Uuid

internal class RemoveUserFromProjectUseCaseImpl(
    private val projectAssignmentsDb: ProjectAssignmentsDb,
) : RemoveUserFromProjectUseCase {
    override suspend operator fun invoke(userId: Uuid, projectId: Uuid) {
        logger.debug("Removing user {} from project {}.", userId, projectId)
        projectAssignmentsDb.removeUser(userId, projectId)
        logger.debug("Removed user {} from project {}.", userId, projectId)
    }
}
