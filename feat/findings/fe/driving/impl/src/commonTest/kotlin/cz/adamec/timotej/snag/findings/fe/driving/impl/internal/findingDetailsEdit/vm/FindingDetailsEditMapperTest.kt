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

package cz.adamec.timotej.snag.findings.fe.driving.impl.internal.findingDetailsEdit.vm

import cz.adamec.timotej.snag.feat.findings.business.FindingType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FindingDetailsEditMapperTest {
    @Test
    fun `canSave is true when can edit finding`() {
        val vmState =
            FindingDetailsEditVmState(
                canEditFinding = true,
            )

        assertTrue(vmState.toUiState().canSave)
    }

    @Test
    fun `canSave is false when cannot edit finding`() {
        val vmState =
            FindingDetailsEditVmState(
                canEditFinding = false,
            )

        assertFalse(vmState.toUiState().canSave)
    }

    @Test
    fun `maps all fields correctly`() {
        val vmState =
            FindingDetailsEditVmState(
                findingName = "Crack",
                findingDescription = "A crack in the wall",
                findingType = FindingType.Classic(),
                findingNameError = null,
                canEditFinding = true,
            )

        val uiState = vmState.toUiState()

        assertEquals("Crack", uiState.findingName)
        assertEquals("A crack in the wall", uiState.findingDescription)
        assertEquals(FindingType.Classic(), uiState.findingType)
        assertEquals(null, uiState.findingNameError)
        assertTrue(uiState.canSave)
    }
}
