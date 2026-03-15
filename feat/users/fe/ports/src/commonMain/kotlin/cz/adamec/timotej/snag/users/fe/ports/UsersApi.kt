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

import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.users.fe.model.FrontendUser
import kotlin.uuid.Uuid

sealed interface UserSyncResult {
    data class Updated(
        val user: FrontendUser,
    ) : UserSyncResult
}

interface UsersApi {
    suspend fun getUsers(): OnlineDataResult<List<FrontendUser>>

    suspend fun getUser(id: Uuid): OnlineDataResult<FrontendUser>

    suspend fun getUsersModifiedSince(since: Timestamp): OnlineDataResult<List<UserSyncResult>>

    suspend fun updateUser(user: FrontendUser): OnlineDataResult<FrontendUser>
}
