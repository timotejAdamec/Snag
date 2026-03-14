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

import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.database.fe.test.FakeDbOps
import cz.adamec.timotej.snag.users.fe.model.FrontendUser
import cz.adamec.timotej.snag.users.fe.ports.UsersDb
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

class FakeUsersDb : UsersDb {
    private val ops = FakeDbOps<FrontendUser>(getId = { it.user.id })

    var forcedFailure
        get() = ops.forcedFailure
        set(value) {
            ops.forcedFailure = value
        }

    override fun getAllUsersFlow(): Flow<OfflineFirstDataResult<List<FrontendUser>>> = ops.allItemsFlow()

    override fun getUserFlow(id: Uuid): Flow<OfflineFirstDataResult<FrontendUser?>> = ops.itemByIdFlow(id)

    override suspend fun saveUser(user: FrontendUser): OfflineFirstDataResult<Unit> = ops.saveOneItem(user)

    override suspend fun saveUsers(users: List<FrontendUser>): OfflineFirstDataResult<Unit> = ops.saveManyItems(users)

    fun setUser(user: FrontendUser) = ops.setItem(user)
}
