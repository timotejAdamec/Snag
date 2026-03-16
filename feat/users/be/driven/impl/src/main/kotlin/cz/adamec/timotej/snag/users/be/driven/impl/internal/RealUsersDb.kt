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

package cz.adamec.timotej.snag.users.be.driven.impl.internal

import cz.adamec.timotej.snag.feat.shared.database.be.UserEntity
import cz.adamec.timotej.snag.feat.shared.database.be.UsersTable
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.users.be.model.BackendUser
import cz.adamec.timotej.snag.users.be.ports.UsersDb
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.uuid.Uuid

internal class RealUsersDb(
    private val database: Database,
) : UsersDb {
    override suspend fun getUsers(): List<BackendUser> =
        transaction(database) {
            UserEntity.all().map { it.toModel() }
        }

    override suspend fun getUser(id: Uuid): BackendUser? =
        transaction(database) {
            UserEntity.findById(id)?.toModel()
        }

    override suspend fun getUserByEntraId(entraId: String): BackendUser? =
        transaction(database) {
            UserEntity.find { UsersTable.entraId eq entraId }.firstOrNull()?.toModel()
        }

    override suspend fun saveUser(user: BackendUser): BackendUser =
        transaction(database) {
            val existing = UserEntity.findById(user.id)
            if (existing != null) {
                existing.entraId = user.entraId
                existing.email = user.email
                existing.role = user.role?.name
                existing.updatedAt = user.updatedAt.value
                existing.toModel()
            } else {
                UserEntity
                    .new(user.id) {
                        entraId = user.entraId
                        email = user.email
                        role = user.role?.name
                        updatedAt = user.updatedAt.value
                    }.toModel()
            }
        }

    override suspend fun getUsersModifiedSince(since: Timestamp): List<BackendUser> =
        transaction(database) {
            UserEntity
                .find { UsersTable.updatedAt greater since.value }
                .map { it.toModel() }
        }
}
