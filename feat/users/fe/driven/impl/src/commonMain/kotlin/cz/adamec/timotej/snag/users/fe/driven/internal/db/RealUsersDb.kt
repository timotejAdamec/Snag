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

package cz.adamec.timotej.snag.users.fe.driven.internal.db

import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.users.app.model.AppUser
import cz.adamec.timotej.snag.users.fe.ports.UsersDb
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

internal class RealUsersDb(
    private val ops: UsersSqlDelightDbOps,
) : UsersDb {
    override fun getAllUsersFlow(): Flow<OfflineFirstDataResult<List<AppUser>>> = ops.allEntitiesFlow()

    override fun getUserFlow(id: Uuid): Flow<OfflineFirstDataResult<AppUser?>> = ops.entityByIdFlow(id)

    override suspend fun saveUser(user: AppUser): OfflineFirstDataResult<Unit> = ops.saveOne(user)

    override suspend fun saveUsers(users: List<AppUser>): OfflineFirstDataResult<Unit> = ops.saveMany(users)
}
