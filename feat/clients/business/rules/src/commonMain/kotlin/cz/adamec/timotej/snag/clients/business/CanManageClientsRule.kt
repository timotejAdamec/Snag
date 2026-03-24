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

package cz.adamec.timotej.snag.clients.business

import cz.adamec.timotej.snag.authorization.business.UserRole
import cz.adamec.timotej.snag.users.business.User

class CanManageClientsRule {
    operator fun invoke(user: User) = user.role in ALLOWED_ROLES

    private companion object {
        val ALLOWED_ROLES =
            setOf(
                UserRole.ADMINISTRATOR,
                UserRole.PASSPORT_LEAD,
                UserRole.SERVICE_LEAD,
                UserRole.SERVICE_WORKER,
            )
    }
}
