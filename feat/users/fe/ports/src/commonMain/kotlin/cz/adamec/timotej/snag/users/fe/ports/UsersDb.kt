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

import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.users.fe.model.FrontendUser
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

interface UsersDb {
    fun getAllUsersFlow(): Flow<OfflineFirstDataResult<List<FrontendUser>>>

    fun getUserFlow(id: Uuid): Flow<OfflineFirstDataResult<FrontendUser?>>

    suspend fun saveUser(user: FrontendUser): OfflineFirstDataResult<Unit>

    suspend fun saveUsers(users: List<FrontendUser>): OfflineFirstDataResult<Unit>
}
