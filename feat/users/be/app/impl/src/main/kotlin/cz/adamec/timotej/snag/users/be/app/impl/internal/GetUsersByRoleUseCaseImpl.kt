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
import cz.adamec.timotej.snag.users.be.app.api.GetUsersByRoleUseCase
import cz.adamec.timotej.snag.users.be.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.users.be.model.BackendUser
import cz.adamec.timotej.snag.users.be.ports.UsersDb

internal class GetUsersByRoleUseCaseImpl(
    private val usersDb: UsersDb,
) : GetUsersByRoleUseCase {
    override suspend operator fun invoke(role: UserRole): List<BackendUser> {
        logger.debug("Getting users with role {} from local storage.", role)
        return usersDb.getUsersByRole(role).also {
            logger.debug("Got {} users with role {} from local storage.", it.size, role)
        }
    }
}
