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

import cz.adamec.timotej.snag.users.be.app.api.UpdateUserRoleUseCase
import cz.adamec.timotej.snag.users.be.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.users.be.model.BackendUser
import cz.adamec.timotej.snag.users.be.ports.UsersDb
import cz.adamec.timotej.snag.users.business.UserRole
import kotlin.uuid.Uuid

internal class UpdateUserRoleUseCaseImpl(
    private val usersDb: UsersDb,
) : UpdateUserRoleUseCase {
    override suspend operator fun invoke(id: Uuid, role: UserRole?): BackendUser? {
        logger.debug("Updating role for user {} to {}.", id, role)
        return usersDb.updateRole(id, role).also {
            logger.debug("Updated role for user {} to {}.", id, role)
        }
    }
}
