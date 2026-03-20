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

import cz.adamec.timotej.snag.users.business.User
import cz.adamec.timotej.snag.users.business.UserRole

class CanCloseProjectRule {
    operator fun invoke(
        user: User,
        project: Project,
    ): Boolean =
        if (user.role == UserRole.ADMINISTRATOR) {
            true
        } else {
            user.id == project.creatorId
        }
}
