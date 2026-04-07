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

package cz.adamec.timotej.snag.feat.inspections.fe.driving.impl.internal.vm

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class InspectionEditMapperTest {
    @Test
    fun `canEdit is true when not deleted and can edit inspection`() {
        val vmState =
            InspectionEditVmState(
                isBeingDeleted = false,
                canEditInspection = true,
            )

        assertTrue(vmState.toUiState().canEdit)
    }

    @Test
    fun `canEdit is false when being deleted`() {
        val vmState =
            InspectionEditVmState(
                isBeingDeleted = true,
                canEditInspection = true,
            )

        assertFalse(vmState.toUiState().canEdit)
    }

    @Test
    fun `canEdit is false when cannot edit inspection`() {
        val vmState =
            InspectionEditVmState(
                isBeingDeleted = false,
                canEditInspection = false,
            )

        assertFalse(vmState.toUiState().canEdit)
    }

    @Test
    fun `canEdit is false when both deleted and cannot edit inspection`() {
        val vmState =
            InspectionEditVmState(
                isBeingDeleted = true,
                canEditInspection = false,
            )

        assertFalse(vmState.toUiState().canEdit)
    }

    @Test
    fun `maps all fields correctly`() {
        val projectId = Uuid.parse("00000000-0000-0000-0000-000000000001")
        val vmState =
            InspectionEditVmState(
                projectId = projectId,
                dateFrom = Timestamp(100L),
                dateTo = Timestamp(200L),
                participants = "John",
                climate = "Sunny",
                note = "Note",
                canEditInspection = true,
                isBeingDeleted = false,
            )

        val uiState = vmState.toUiState()

        assertEquals(Timestamp(100L), uiState.dateFrom)
        assertEquals(Timestamp(200L), uiState.dateTo)
        assertEquals("John", uiState.participants)
        assertEquals("Sunny", uiState.climate)
        assertEquals("Note", uiState.note)
        assertTrue(uiState.canEdit)
    }
}
