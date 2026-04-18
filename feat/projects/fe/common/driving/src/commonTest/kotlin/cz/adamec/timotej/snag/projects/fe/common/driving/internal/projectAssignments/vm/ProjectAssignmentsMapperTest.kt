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

package cz.adamec.timotej.snag.projects.fe.common.driving.internal.projectAssignments.vm

import cz.adamec.timotej.snag.authorization.business.UserRole
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.users.app.model.AppUserData
import kotlinx.collections.immutable.toPersistentList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class ProjectAssignmentsMapperTest {
    private val userId1 = Uuid.parse("00000000-0000-0000-0000-000000000001")
    private val userId2 = Uuid.parse("00000000-0000-0000-0000-000000000002")
    private val userId3 = Uuid.parse("00000000-0000-0000-0000-000000000003")

    private fun makeUser(
        id: Uuid,
        email: String,
        role: UserRole? = null,
    ) = AppUserData(
        id = id,
        authProviderId = "auth-provider-user-$id",
        email = email,
        role = role,
        updatedAt = Timestamp(0L),
    )

    @Test
    fun `assignedUsers contains only users whose ids are in assignedUserIds`() {
        val vmState =
            ProjectAssignmentsVmState(
                allUsers =
                    listOf(
                        makeUser(id = userId1, email = "user1@example.com"),
                        makeUser(id = userId2, email = "user2@example.com"),
                        makeUser(id = userId3, email = "user3@example.com"),
                    ).toPersistentList(),
                assignedUserIds = setOf(userId1, userId3),
                usersLoaded = true,
                assignmentsLoaded = true,
            )

        val uiState = vmState.toUiState()

        assertEquals(2, uiState.assignedUsers.size)
        assertTrue(uiState.assignedUsers.any { it.id == userId1 })
        assertTrue(uiState.assignedUsers.any { it.id == userId3 })
        assertFalse(uiState.assignedUsers.any { it.id == userId2 })
    }

    @Test
    fun `availableUsers contains only users whose ids are not in assignedUserIds`() {
        val vmState =
            ProjectAssignmentsVmState(
                allUsers =
                    listOf(
                        makeUser(id = userId1, email = "user1@example.com"),
                        makeUser(id = userId2, email = "user2@example.com"),
                        makeUser(id = userId3, email = "user3@example.com"),
                    ).toPersistentList(),
                assignedUserIds = setOf(userId1),
                usersLoaded = true,
                assignmentsLoaded = true,
            )

        val uiState = vmState.toUiState()

        assertEquals(2, uiState.availableUsers.size)
        assertTrue(uiState.availableUsers.any { it.id == userId2 })
        assertTrue(uiState.availableUsers.any { it.id == userId3 })
        assertFalse(uiState.availableUsers.any { it.id == userId1 })
    }

    @Test
    fun `canManageAssignments passes through from VmState`() {
        val vmState =
            ProjectAssignmentsVmState(
                canManageAssignments = true,
                usersLoaded = true,
                assignmentsLoaded = true,
            )

        assertTrue(vmState.toUiState().canManageAssignments)
    }

    @Test
    fun `canManageAssignments is false when false in VmState`() {
        val vmState =
            ProjectAssignmentsVmState(
                canManageAssignments = false,
                usersLoaded = true,
                assignmentsLoaded = true,
            )

        assertFalse(vmState.toUiState().canManageAssignments)
    }

    @Test
    fun `isLoading is true when users not yet loaded`() {
        val vmState =
            ProjectAssignmentsVmState(
                usersLoaded = false,
                assignmentsLoaded = true,
            )

        assertTrue(vmState.toUiState().isLoading)
    }

    @Test
    fun `isLoading is true when assignments not yet loaded`() {
        val vmState =
            ProjectAssignmentsVmState(
                usersLoaded = true,
                assignmentsLoaded = false,
            )

        assertTrue(vmState.toUiState().isLoading)
    }

    @Test
    fun `isLoading is false when both users and assignments are loaded`() {
        val vmState =
            ProjectAssignmentsVmState(
                usersLoaded = true,
                assignmentsLoaded = true,
            )

        assertFalse(vmState.toUiState().isLoading)
    }

    @Test
    fun `AppUser is correctly mapped to AssignedUserItem`() {
        val user =
            makeUser(
                id = userId1,
                email = "test@example.com",
                role = UserRole.ADMINISTRATOR,
            )

        val item = user.toAssignedUserItem()

        assertEquals(userId1, item.id)
        assertEquals("test@example.com", item.email)
        assertEquals(UserRole.ADMINISTRATOR, item.role)
    }

    @Test
    fun `AppUser with null role maps to AssignedUserItem with null role`() {
        val user = makeUser(id = userId1, email = "test@example.com", role = null)

        val item = user.toAssignedUserItem()

        assertEquals(null, item.role)
    }
}
