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

import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class UserManagementMapperTest {

    @Test
    fun `isRoleChangeEnabled is true when not updating role`() {
        val vmState =
            UserManagementVmState(
                users =
                    persistentListOf(
                        UserVmItem(
                            id = Uuid.random(),
                            email = "test@test.com",
                            role = null,
                            isUpdatingRole = false,
                        ),
                    ),
            )

        assertTrue(vmState.toUiState().users.first().isRoleChangeEnabled)
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
                        ),
                    ),
            )

        assertFalse(vmState.toUiState().users.first().isRoleChangeEnabled)
    }

    @Test
    fun `isLoading passes through`() {
        val vmState = UserManagementVmState(isLoading = true)

        assertTrue(vmState.toUiState().isLoading)
    }
}
