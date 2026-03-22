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

package cz.adamec.timotej.snag.structures.fe.driving.impl.internal.floorPlan.vm

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StructureDetailsMapperTest {
    @Test
    fun `canEdit is true when loaded and not deleted and can edit structure`() {
        val vmState =
            StructureDetailsVmState(
                status = StructureDetailsUiStatus.LOADED,
                isBeingDeleted = false,
                canEditStructure = true,
            )

        assertTrue(vmState.toUiState().canEdit)
    }

    @Test
    fun `canEdit is false when loading`() {
        val vmState =
            StructureDetailsVmState(
                status = StructureDetailsUiStatus.LOADING,
                isBeingDeleted = false,
                canEditStructure = true,
            )

        assertFalse(vmState.toUiState().canEdit)
    }

    @Test
    fun `canEdit is false when being deleted`() {
        val vmState =
            StructureDetailsVmState(
                status = StructureDetailsUiStatus.LOADED,
                isBeingDeleted = true,
                canEditStructure = true,
            )

        assertFalse(vmState.toUiState().canEdit)
    }

    @Test
    fun `canEdit is false when cannot edit structure`() {
        val vmState =
            StructureDetailsVmState(
                status = StructureDetailsUiStatus.LOADED,
                isBeingDeleted = false,
                canEditStructure = false,
            )

        assertFalse(vmState.toUiState().canEdit)
    }

    @Test
    fun `canEdit is false when status is error`() {
        val vmState =
            StructureDetailsVmState(
                status = StructureDetailsUiStatus.ERROR,
                isBeingDeleted = false,
                canEditStructure = true,
            )

        assertFalse(vmState.toUiState().canEdit)
    }

    @Test
    fun `maps status and findings correctly`() {
        val vmState =
            StructureDetailsVmState(
                status = StructureDetailsUiStatus.NOT_FOUND,
            )

        val uiState = vmState.toUiState()

        assertEquals(StructureDetailsUiStatus.NOT_FOUND, uiState.status)
        assertNull(uiState.feStructure)
    }
}
