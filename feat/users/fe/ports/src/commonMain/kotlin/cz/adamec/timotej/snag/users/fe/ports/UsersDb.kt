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

package cz.adamec.timotej.snag.users.fe.ports

import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.users.app.model.AppUser
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

interface UsersDb {
    fun getAllUsersFlow(): Flow<OfflineFirstDataResult<List<AppUser>>>

    fun getUserFlow(id: Uuid): Flow<OfflineFirstDataResult<AppUser?>>

    fun getUserByAuthProviderIdFlow(authProviderId: String): Flow<OfflineFirstDataResult<AppUser?>>

    suspend fun saveUser(user: AppUser): OfflineFirstDataResult<Unit>

    suspend fun saveUsers(users: List<AppUser>): OfflineFirstDataResult<Unit>
}
