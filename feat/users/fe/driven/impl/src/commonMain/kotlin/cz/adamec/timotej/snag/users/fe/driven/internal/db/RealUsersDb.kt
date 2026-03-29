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

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.feat.shared.database.fe.db.UserEntityQueries
import cz.adamec.timotej.snag.users.app.model.AppUser
import cz.adamec.timotej.snag.users.fe.ports.UsersDb
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlin.uuid.Uuid

internal class RealUsersDb(
    private val ops: UsersSqlDelightDbOps,
    private val queries: UserEntityQueries,
    private val ioDispatcher: CoroutineDispatcher,
) : UsersDb {
    override fun getAllUsersFlow(): Flow<OfflineFirstDataResult<List<AppUser>>> = ops.allEntitiesFlow()

    override fun getUserFlow(id: Uuid): Flow<OfflineFirstDataResult<AppUser?>> = ops.entityByIdFlow(id)

    override fun getUserByAuthProviderIdFlow(authProviderId: String): Flow<OfflineFirstDataResult<AppUser?>> {
        val query = queries.selectByAuthProviderId(authProviderId)
        return query
            .asFlow()
            .mapToOneOrNull(ioDispatcher)
            .map<_, OfflineFirstDataResult<AppUser?>> { entity ->
                OfflineFirstDataResult.Success(entity?.toModel())
            }.catch { e ->
                emit(OfflineFirstDataResult.ProgrammerError(throwable = e))
            }
    }

    override suspend fun saveUser(user: AppUser): OfflineFirstDataResult<Unit> = ops.saveOne(user)

    override suspend fun saveUsers(users: List<AppUser>): OfflineFirstDataResult<Unit> = ops.saveMany(users)
}
