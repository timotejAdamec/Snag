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

package cz.adamec.timotej.snag.structures.fe.driving.impl.internal.structureDetailsEdit.vm

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StructureDetailsEditMapperTest {
    @Test
    fun `canSave is true when not uploading and can edit structure`() {
        val vmState =
            StructureDetailsEditVmState(
                isUploadingImage = false,
                canEditStructure = true,
            )

        assertTrue(vmState.toUiState().canSave)
    }

    @Test
    fun `canSave is false when uploading`() {
        val vmState =
            StructureDetailsEditVmState(
                isUploadingImage = true,
                canEditStructure = true,
            )

        assertFalse(vmState.toUiState().canSave)
    }

    @Test
    fun `canSave is false when cannot edit structure`() {
        val vmState =
            StructureDetailsEditVmState(
                isUploadingImage = false,
                canEditStructure = false,
            )

        assertFalse(vmState.toUiState().canSave)
    }

    @Test
    fun `canModifyImage is true when both allowed and can edit structure`() {
        val vmState =
            StructureDetailsEditVmState(
                canModifyFloorPlanImage = true,
                canEditStructure = true,
            )

        assertTrue(vmState.toUiState().canModifyImage)
    }

    @Test
    fun `canModifyImage is false when floor plan modification not allowed`() {
        val vmState =
            StructureDetailsEditVmState(
                canModifyFloorPlanImage = false,
                canEditStructure = true,
            )

        assertFalse(vmState.toUiState().canModifyImage)
    }

    @Test
    fun `canModifyImage is false when cannot edit structure`() {
        val vmState =
            StructureDetailsEditVmState(
                canModifyFloorPlanImage = true,
                canEditStructure = false,
            )

        assertFalse(vmState.toUiState().canModifyImage)
    }
}
