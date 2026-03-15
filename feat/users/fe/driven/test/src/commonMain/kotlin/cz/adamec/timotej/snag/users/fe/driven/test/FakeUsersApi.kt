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

import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.network.fe.test.FakeApiOps
import cz.adamec.timotej.snag.users.fe.model.FrontendUser
import cz.adamec.timotej.snag.users.fe.ports.UserSyncResult
import cz.adamec.timotej.snag.users.fe.ports.UsersApi
import kotlin.uuid.Uuid

class FakeUsersApi : UsersApi {
    private val ops = FakeApiOps<FrontendUser, UserSyncResult>(getId = { it.user.id })

    var forcedFailure
        get() = ops.forcedFailure
        set(value) {
            ops.forcedFailure = value
        }

    var modifiedSinceResults
        get() = ops.modifiedSinceResults
        set(value) {
            ops.modifiedSinceResults = value
        }

    override suspend fun getUsers(): OnlineDataResult<List<FrontendUser>> = ops.getAllItems()

    override suspend fun getUser(id: Uuid): OnlineDataResult<FrontendUser> = ops.getItemById(id)

    override suspend fun getUsersModifiedSince(since: Timestamp): OnlineDataResult<List<UserSyncResult>> = ops.getModifiedSinceItems()

    override suspend fun updateUser(user: FrontendUser): OnlineDataResult<FrontendUser> {
        forcedFailure?.let { return it }
        ops.setItem(user)
        return OnlineDataResult.Success(user)
    }

    fun setUser(user: FrontendUser) = ops.setItem(user)
}
