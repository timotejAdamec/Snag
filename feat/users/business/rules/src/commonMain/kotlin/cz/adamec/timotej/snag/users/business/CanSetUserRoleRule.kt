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

package cz.adamec.timotej.snag.users.business

import cz.adamec.timotej.snag.authorization.business.UserRole

class CanSetUserRoleRule {
    operator fun invoke(
        actingUser: User,
        targetCurrentRole: UserRole?,
        newRole: UserRole?,
    ): Boolean =
        when (actingUser.role) {
            UserRole.ADMINISTRATOR -> true
            UserRole.PASSPORT_LEAD -> isTransitionBetween(
                targetCurrentRole = targetCurrentRole,
                newRole = newRole,
                delegatableRole = UserRole.PASSPORT_TECHNICIAN,
            )
            UserRole.SERVICE_LEAD -> isTransitionBetween(
                targetCurrentRole = targetCurrentRole,
                newRole = newRole,
                delegatableRole = UserRole.SERVICE_WORKER,
            )
            else -> false
        }

    private fun isTransitionBetween(
        targetCurrentRole: UserRole?,
        newRole: UserRole?,
        delegatableRole: UserRole,
    ): Boolean {
        val isAssigning = targetCurrentRole == null && newRole == delegatableRole
        val isRemoving = targetCurrentRole == delegatableRole && newRole == null
        return isAssigning || isRemoving
    }
}
