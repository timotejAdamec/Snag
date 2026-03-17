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

package cz.adamec.timotej.snag.users.be.ports

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.users.be.model.BackendUser
import kotlin.uuid.Uuid

interface UsersDb {
    suspend fun getUsers(): List<BackendUser>

    suspend fun getUser(id: Uuid): BackendUser?

    suspend fun getUserByEntraId(entraId: String): BackendUser?

    suspend fun saveUser(user: BackendUser): BackendUser

    suspend fun getUsersModifiedSince(since: Timestamp): List<BackendUser>
}
