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

package cz.adamec.timotej.snag.projects.business

import cz.adamec.timotej.snag.authorization.business.UserRole
import cz.adamec.timotej.snag.users.business.User
import kotlin.uuid.Uuid

class CanAccessProjectRule {
    operator fun invoke(
        user: User,
        project: Project,
        assignedUserIds: Set<Uuid>,
    ): Boolean =
        when {
            user.role == UserRole.ADMINISTRATOR -> true
            user.id == project.creatorId -> true
            user.id in assignedUserIds -> true
            else -> false
        }
}
