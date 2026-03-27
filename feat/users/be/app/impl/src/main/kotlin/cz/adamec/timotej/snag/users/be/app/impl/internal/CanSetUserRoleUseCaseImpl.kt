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

import cz.adamec.timotej.snag.authorization.business.UserRole
import cz.adamec.timotej.snag.users.be.app.api.CanSetUserRoleUseCase
import cz.adamec.timotej.snag.users.be.app.api.GetUserUseCase
import cz.adamec.timotej.snag.users.business.CanSetUserRoleRule
import kotlin.uuid.Uuid

internal class CanSetUserRoleUseCaseImpl(
    private val getUserUseCase: GetUserUseCase,
    private val canSetUserRoleRule: CanSetUserRoleRule,
) : CanSetUserRoleUseCase {
    override suspend operator fun invoke(
        actingUserId: Uuid,
        targetUserId: Uuid,
        newRole: UserRole?,
    ): Boolean {
        val actingUser = getUserUseCase(actingUserId) ?: return false
        val targetUser = getUserUseCase(targetUserId) ?: return false
        return canSetUserRoleRule(
            actingUser = actingUser,
            targetCurrentRole = targetUser.role,
            newRole = newRole,
        )
    }
}
