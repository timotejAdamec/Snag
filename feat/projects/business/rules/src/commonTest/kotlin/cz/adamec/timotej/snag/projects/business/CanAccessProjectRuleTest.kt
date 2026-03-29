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

package cz.adamec.timotej.snag.projects.business

import cz.adamec.timotej.snag.authorization.business.UserRole
import cz.adamec.timotej.snag.core.foundation.common.UuidProvider
import cz.adamec.timotej.snag.users.business.User
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class CanAccessProjectRuleTest {
    private val rule = CanAccessProjectRule()

    private val creatorId = UuidProvider.getUuid()
    private val assignedUserId = UuidProvider.getUuid()
    private val otherUserId = UuidProvider.getUuid()

    private fun createUser(
        id: Uuid = otherUserId,
        role: UserRole?,
    ) = object : User {
        override val id: Uuid = id
        override val email: String = "test@example.com"
        override val role: UserRole? = role
    }

    private fun createProject(projectCreatorId: Uuid = creatorId) =
        object : Project {
            override val id: Uuid = UuidProvider.getUuid()
            override val name: String = "Test Project"
            override val address: String = "Test Address"
            override val clientId: Uuid? = null
            override val creatorId: Uuid = projectCreatorId
            override val isClosed: Boolean = false
        }

    @Test
    fun `ADMINISTRATOR can access any project`() {
        val user = createUser(role = UserRole.ADMINISTRATOR)
        assertTrue(
            rule(
                user = user,
                project = createProject(),
                assignedUserIds = emptySet(),
            ),
        )
    }

    @Test
    fun `creator can access own project`() {
        val user = createUser(id = creatorId, role = UserRole.PASSPORT_LEAD)
        assertTrue(
            rule(
                user = user,
                project = createProject(projectCreatorId = creatorId),
                assignedUserIds = emptySet(),
            ),
        )
    }

    @Test
    fun `assigned user can access project`() {
        val user = createUser(id = assignedUserId, role = UserRole.PASSPORT_TECHNICIAN)
        assertTrue(
            rule(
                user = user,
                project = createProject(),
                assignedUserIds = setOf(assignedUserId),
            ),
        )
    }

    @Test
    fun `non-creator non-assigned non-admin cannot access project`() {
        val user = createUser(id = otherUserId, role = UserRole.PASSPORT_LEAD)
        assertFalse(
            rule(
                user = user,
                project = createProject(),
                assignedUserIds = emptySet(),
            ),
        )
    }

    @Test
    fun `user with null role cannot access project they did not create`() {
        val user = createUser(id = otherUserId, role = null)
        assertFalse(
            rule(
                user = user,
                project = createProject(),
                assignedUserIds = emptySet(),
            ),
        )
    }

    @Test
    fun `user with null role can access project they created`() {
        val user = createUser(id = creatorId, role = null)
        assertTrue(
            rule(
                user = user,
                project = createProject(projectCreatorId = creatorId),
                assignedUserIds = emptySet(),
            ),
        )
    }
}
