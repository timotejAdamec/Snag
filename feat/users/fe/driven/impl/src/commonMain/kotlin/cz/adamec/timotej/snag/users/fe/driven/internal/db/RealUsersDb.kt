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
import cz.adamec.timotej.snag.users.fe.model.FrontendUser
import cz.adamec.timotej.snag.users.fe.ports.UsersDb
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

internal class RealUsersDb(
    private val ops: UsersSqlDelightDbOps,
) : UsersDb {
    override fun getAllUsersFlow(): Flow<OfflineFirstDataResult<List<FrontendUser>>> = ops.allEntitiesFlow()

    override fun getUserFlow(id: Uuid): Flow<OfflineFirstDataResult<FrontendUser?>> = ops.entityByIdFlow(id)

    override suspend fun saveUser(user: FrontendUser): OfflineFirstDataResult<Unit> = ops.saveOne(user)

    override suspend fun saveUsers(users: List<FrontendUser>): OfflineFirstDataResult<Unit> = ops.saveMany(users)
}
