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

package cz.adamec.timotej.snag.findings.fe.driving.impl.internal.findingDetail.vm

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FindingDetailMapperTest {
    @Test
    fun `canEdit is true when loaded and not deleted and can edit finding`() {
        val vmState =
            FindingDetailVmState(
                status = FindingDetailUiStatus.LOADED,
                isBeingDeleted = false,
                canEditFinding = true,
            )

        assertTrue(vmState.toUiState().canEdit)
    }

    @Test
    fun `canEdit is false when loading`() {
        val vmState =
            FindingDetailVmState(
                status = FindingDetailUiStatus.LOADING,
                isBeingDeleted = false,
                canEditFinding = true,
            )

        assertFalse(vmState.toUiState().canEdit)
    }

    @Test
    fun `canEdit is false when being deleted`() {
        val vmState =
            FindingDetailVmState(
                status = FindingDetailUiStatus.LOADED,
                isBeingDeleted = true,
                canEditFinding = true,
            )

        assertFalse(vmState.toUiState().canEdit)
    }

    @Test
    fun `canEdit is false when cannot edit finding`() {
        val vmState =
            FindingDetailVmState(
                status = FindingDetailUiStatus.LOADED,
                isBeingDeleted = false,
                canEditFinding = false,
            )

        assertFalse(vmState.toUiState().canEdit)
    }

    @Test
    fun `canEdit is false when status is error`() {
        val vmState =
            FindingDetailVmState(
                status = FindingDetailUiStatus.ERROR,
                isBeingDeleted = false,
                canEditFinding = true,
            )

        assertFalse(vmState.toUiState().canEdit)
    }

    @Test
    fun `maps status and finding correctly`() {
        val vmState =
            FindingDetailVmState(
                status = FindingDetailUiStatus.NOT_FOUND,
                finding = null,
            )

        val uiState = vmState.toUiState()

        assertEquals(FindingDetailUiStatus.NOT_FOUND, uiState.status)
        assertNull(uiState.finding)
    }
}
