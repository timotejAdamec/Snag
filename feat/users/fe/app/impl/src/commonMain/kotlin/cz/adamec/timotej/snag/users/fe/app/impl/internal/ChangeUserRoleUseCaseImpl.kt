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

package cz.adamec.timotej.snag.users.fe.app.impl.internal

import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.users.business.UserRole
import cz.adamec.timotej.snag.users.fe.app.api.ChangeUserRoleUseCase
import cz.adamec.timotej.snag.users.fe.model.FrontendUser
import cz.adamec.timotej.snag.users.fe.ports.UsersApi
import cz.adamec.timotej.snag.users.fe.ports.UsersDb

class ChangeUserRoleUseCaseImpl(
    private val usersApi: UsersApi,
    private val usersDb: UsersDb,
) : ChangeUserRoleUseCase {
    override suspend fun invoke(
        user: FrontendUser,
        newRole: UserRole?,
    ): OnlineDataResult<FrontendUser> {
        val updatedUser =
            FrontendUser(
                user = user.user.copy(role = newRole),
            )
        return when (val result = usersApi.updateUser(updatedUser)) {
            is OnlineDataResult.Success -> {
                usersDb.saveUser(result.data)
                result
            }
            is OnlineDataResult.Failure -> {
                result
            }
        }
    }
}
