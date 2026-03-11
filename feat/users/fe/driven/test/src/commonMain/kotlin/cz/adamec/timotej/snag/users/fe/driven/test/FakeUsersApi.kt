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

package cz.adamec.timotej.snag.users.fe.driven.test

import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.users.fe.model.FrontendUser
import cz.adamec.timotej.snag.users.fe.ports.UsersApi
import kotlin.uuid.Uuid

class FakeUsersApi : UsersApi {
    var users = mutableListOf<FrontendUser>()
    var forcedFailure: Exception? = null
    var currentUser: FrontendUser? = null

    override suspend fun getUsers(): OnlineDataResult<List<FrontendUser>> =
        forcedFailure?.let { OnlineDataResult.Error(it) }
            ?: OnlineDataResult.Success(users.toList())

    override suspend fun getUser(id: Uuid): OnlineDataResult<FrontendUser> =
        forcedFailure?.let { OnlineDataResult.Error(it) }
            ?: users.find { it.user.id == id }?.let { OnlineDataResult.Success(it) }
            ?: OnlineDataResult.Error(NoSuchElementException("User not found"))

    override suspend fun getCurrentUser(): OnlineDataResult<FrontendUser> =
        forcedFailure?.let { OnlineDataResult.Error(it) }
            ?: currentUser?.let { OnlineDataResult.Success(it) }
            ?: OnlineDataResult.Error(NoSuchElementException("Current user not set"))

    fun setUser(user: FrontendUser) {
        users.removeAll { it.user.id == user.user.id }
        users.add(user)
    }
}
