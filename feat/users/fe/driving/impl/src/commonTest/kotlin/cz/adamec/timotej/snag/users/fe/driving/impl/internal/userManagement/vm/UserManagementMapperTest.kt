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

package cz.adamec.timotej.snag.users.fe.driving.impl.internal.userManagement.vm

import cz.adamec.timotej.snag.authorization.business.UserRole
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class UserManagementMapperTest {
    @Test
    fun `isRoleChangeEnabled is true when not updating role and allowedRoleOptions is not empty`() {
        val vmState =
            UserManagementVmState(
                users =
                    persistentListOf(
                        UserVmItem(
                            id = Uuid.random(),
                            email = "test@test.com",
                            role = null,
                            isUpdatingRole = false,
                            allowedRoleOptions = setOf(UserRole.PASSPORT_TECHNICIAN),
                        ),
                    ),
            )

        assertTrue(
            vmState
                .toUiState()
                .users
                .first()
                .isRoleChangeEnabled,
        )
    }

    @Test
    fun `isRoleChangeEnabled is false when updating role`() {
        val vmState =
            UserManagementVmState(
                users =
                    persistentListOf(
                        UserVmItem(
                            id = Uuid.random(),
                            email = "test@test.com",
                            role = null,
                            isUpdatingRole = true,
                            allowedRoleOptions = setOf(UserRole.PASSPORT_TECHNICIAN),
                        ),
                    ),
            )

        assertFalse(
            vmState
                .toUiState()
                .users
                .first()
                .isRoleChangeEnabled,
        )
    }

    @Test
    fun `isRoleChangeEnabled is false when allowedRoleOptions is empty`() {
        val vmState =
            UserManagementVmState(
                users =
                    persistentListOf(
                        UserVmItem(
                            id = Uuid.random(),
                            email = "test@test.com",
                            role = null,
                            isUpdatingRole = false,
                            allowedRoleOptions = emptySet(),
                        ),
                    ),
            )

        assertFalse(
            vmState
                .toUiState()
                .users
                .first()
                .isRoleChangeEnabled,
        )
    }

    @Test
    fun `allowedRoleOptions passes through to UiState`() {
        val options = setOf(UserRole.PASSPORT_TECHNICIAN, UserRole.SERVICE_WORKER)
        val vmState =
            UserManagementVmState(
                users =
                    persistentListOf(
                        UserVmItem(
                            id = Uuid.random(),
                            email = "test@test.com",
                            role = null,
                            allowedRoleOptions = options,
                        ),
                    ),
            )

        assertEquals(
            options,
            vmState
                .toUiState()
                .users
                .first()
                .allowedRoleOptions,
        )
    }

    @Test
    fun `isLoading passes through`() {
        val vmState = UserManagementVmState(isLoading = true)

        assertTrue(vmState.toUiState().isLoading)
    }
}
