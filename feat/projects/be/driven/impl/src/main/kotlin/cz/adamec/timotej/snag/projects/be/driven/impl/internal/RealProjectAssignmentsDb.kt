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

package cz.adamec.timotej.snag.projects.be.driven.impl.internal

import cz.adamec.timotej.snag.feat.shared.database.be.ProjectAssignmentsTable
import cz.adamec.timotej.snag.feat.shared.database.be.UserEntity
import cz.adamec.timotej.snag.feat.shared.database.be.UsersTable
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.projects.be.ports.ProjectAssignmentsDb
import cz.adamec.timotej.snag.users.be.model.BackendUser
import cz.adamec.timotej.snag.users.business.User
import cz.adamec.timotej.snag.users.business.UserRole
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertIgnore
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.uuid.Uuid

internal class RealProjectAssignmentsDb(
    private val database: Database,
) : ProjectAssignmentsDb {
    override suspend fun getAssignedUsers(projectId: Uuid): List<BackendUser> =
        transaction(database) {
            val userIds =
                ProjectAssignmentsTable
                    .selectAll()
                    .where { ProjectAssignmentsTable.projectId eq projectId }
                    .map { it[ProjectAssignmentsTable.userId] }

            UserEntity
                .find { UsersTable.id inList userIds }
                .map { it.toBackendUser() }
        }

    override suspend fun assignUser(
        userId: Uuid,
        projectId: Uuid,
    ) {
        transaction(database) {
            ProjectAssignmentsTable.insertIgnore {
                it[ProjectAssignmentsTable.userId] = userId
                it[ProjectAssignmentsTable.projectId] = projectId
            }
        }
    }

    override suspend fun removeUser(
        userId: Uuid,
        projectId: Uuid,
    ) {
        transaction(database) {
            ProjectAssignmentsTable.deleteWhere {
                ProjectAssignmentsTable.userId eq userId and
                    (ProjectAssignmentsTable.projectId eq projectId)
            }
        }
    }

    override suspend fun getProjectsForUser(userId: Uuid): List<Uuid> =
        transaction(database) {
            ProjectAssignmentsTable
                .selectAll()
                .where { ProjectAssignmentsTable.userId eq userId }
                .map { it[ProjectAssignmentsTable.projectId] }
        }
}

private fun UserEntity.toBackendUser() =
    BackendUser(
        user =
            User(
                id = id.value,
                entraId = entraId,
                email = email,
                role = role?.let { UserRole.valueOf(it) },
                updatedAt = Timestamp(updatedAt),
            ),
    )
