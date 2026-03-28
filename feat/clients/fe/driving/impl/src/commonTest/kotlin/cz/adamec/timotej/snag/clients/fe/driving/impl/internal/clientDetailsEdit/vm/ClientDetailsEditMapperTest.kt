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

package cz.adamec.timotej.snag.clients.fe.driving.impl.internal.clientDetailsEdit.vm

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ClientDetailsEditMapperTest {
    @Test
    fun `canDelete is true when canDelete is true and canManageClients is true and not being deleted`() {
        val vmState =
            ClientDetailsEditVmState(
                canDelete = true,
                isBeingDeleted = false,
                canManageClients = true,
            )

        assertTrue(vmState.toUiState().canDelete)
    }

    @Test
    fun `canDelete is false when being deleted even if canDelete is true`() {
        val vmState =
            ClientDetailsEditVmState(
                canDelete = true,
                isBeingDeleted = true,
                canManageClients = true,
            )

        assertFalse(vmState.toUiState().canDelete)
    }

    @Test
    fun `canDelete is false when canDelete from use case is false`() {
        val vmState =
            ClientDetailsEditVmState(
                canDelete = false,
                isBeingDeleted = false,
                canManageClients = true,
            )

        assertFalse(vmState.toUiState().canDelete)
    }

    @Test
    fun `canDelete is false when canManageClients is false`() {
        val vmState =
            ClientDetailsEditVmState(
                canDelete = true,
                isBeingDeleted = false,
                canManageClients = false,
            )

        assertFalse(vmState.toUiState().canDelete)
    }

    @Test
    fun `areDeleteDialogButtonsEnabled is true when not being deleted`() {
        val vmState =
            ClientDetailsEditVmState(
                isBeingDeleted = false,
            )

        assertTrue(vmState.toUiState().areDeleteDialogButtonsEnabled)
    }

    @Test
    fun `areDeleteDialogButtonsEnabled is false when being deleted`() {
        val vmState =
            ClientDetailsEditVmState(
                isBeingDeleted = true,
            )

        assertFalse(vmState.toUiState().areDeleteDialogButtonsEnabled)
    }

    @Test
    fun `canSave is true when canManageClients is true`() {
        assertTrue(ClientDetailsEditVmState(canManageClients = true).toUiState().canSave)
    }

    @Test
    fun `canSave is false when canManageClients is false`() {
        assertFalse(ClientDetailsEditVmState(canManageClients = false).toUiState().canSave)
    }
}
