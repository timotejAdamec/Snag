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

import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.users.app.model.AppUser
import cz.adamec.timotej.snag.users.app.model.AppUserData
import cz.adamec.timotej.snag.users.fe.app.api.ChangeUserRoleUseCase
import cz.adamec.timotej.snag.users.fe.app.api.model.ChangeUserRoleRequest
import cz.adamec.timotej.snag.users.fe.ports.UsersApi
import cz.adamec.timotej.snag.users.fe.ports.UsersDb
import kotlinx.coroutines.flow.first

class ChangeUserRoleUseCaseImpl(
    private val usersApi: UsersApi,
    private val usersDb: UsersDb,
) : ChangeUserRoleUseCase {
    override suspend fun invoke(request: ChangeUserRoleRequest): OnlineDataResult<AppUser> {
        val userResult = usersDb.getUserFlow(request.userId).first()
        return when (userResult) {
            is OfflineFirstDataResult.ProgrammerError ->
                OnlineDataResult.Failure.ProgrammerError(userResult.throwable)
            is OfflineFirstDataResult.Success ->
                updateUser(request, userResult.data)
        }
    }

    private suspend fun updateUser(
        request: ChangeUserRoleRequest,
        user: AppUser?,
    ): OnlineDataResult<AppUser> {
        if (user == null) {
            return OnlineDataResult.Failure.ProgrammerError(
                IllegalStateException("User ${request.userId} not found in DB"),
            )
        }
        val updatedUser =
            AppUserData(
                id = user.id,
                authProviderId = user.authProviderId,
                email = user.email,
                role = request.newRole,
                updatedAt = user.updatedAt,
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
