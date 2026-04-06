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

package cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projectDetails.vm

import cz.adamec.timotej.snag.authorization.business.UserRole
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.foundation.common.UuidProvider
import cz.adamec.timotej.snag.projects.app.model.AppProjectData
import cz.adamec.timotej.snag.users.app.model.AppUserData
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class ProjectDetailsMapperTest {
    private fun openProject() =
        AppProjectData(
            id = Uuid.random(),
            name = "Test",
            address = "Address",
            creatorId = UuidProvider.getUuid(),
            updatedAt = Timestamp(0L),
            isClosed = false,
        )

    private fun closedProject() =
        AppProjectData(
            id = Uuid.random(),
            name = "Test",
            address = "Address",
            creatorId = UuidProvider.getUuid(),
            updatedAt = Timestamp(0L),
            isClosed = true,
        )

    @Test
    fun `isClosed is true when project isClosed is true`() {
        val vmState =
            ProjectDetailsVmState(
                project = closedProject(),
            )

        assertTrue(vmState.toUiState().isClosed)
    }

    @Test
    fun `isClosed is false when project is null`() {
        val vmState = ProjectDetailsVmState(project = null)

        assertFalse(vmState.toUiState().isClosed)
    }

    @Test
    fun `isClosed is false when project isClosed is false`() {
        val vmState =
            ProjectDetailsVmState(
                project = openProject(),
            )

        assertFalse(vmState.toUiState().isClosed)
    }

    @Test
    fun `isProjectEditable is true when LOADED and not closed and canEditEntities is true`() {
        val vmState =
            ProjectDetailsVmState(
                projectStatus = ProjectDetailsUiStatus.LOADED,
                project = openProject(),
                canEditEntities = true,
            )

        assertTrue(vmState.toUiState().isProjectEditable)
    }

    @Test
    fun `isProjectEditable is false when LOADING`() {
        val vmState =
            ProjectDetailsVmState(
                projectStatus = ProjectDetailsUiStatus.LOADING,
                project = openProject(),
                canEditEntities = true,
            )

        assertFalse(vmState.toUiState().isProjectEditable)
    }

    @Test
    fun `isProjectEditable is false when project is closed`() {
        val vmState =
            ProjectDetailsVmState(
                projectStatus = ProjectDetailsUiStatus.LOADED,
                project = closedProject(),
                canEditEntities = true,
            )

        assertFalse(vmState.toUiState().isProjectEditable)
    }

    @Test
    fun `isProjectEditable is false when canEditEntities is false`() {
        val vmState =
            ProjectDetailsVmState(
                projectStatus = ProjectDetailsUiStatus.LOADED,
                project = openProject(),
                canEditEntities = false,
            )

        assertFalse(vmState.toUiState().isProjectEditable)
    }

    @Test
    fun `canInvokeDeletion is true when editable and not being deleted`() {
        val vmState =
            ProjectDetailsVmState(
                projectStatus = ProjectDetailsUiStatus.LOADED,
                project = openProject(),
                isBeingDeleted = false,
                canEditEntities = true,
            )

        assertTrue(vmState.toUiState().canInvokeDeletion)
    }

    @Test
    fun `canInvokeDeletion is false when being deleted`() {
        val vmState =
            ProjectDetailsVmState(
                projectStatus = ProjectDetailsUiStatus.LOADED,
                project = openProject(),
                isBeingDeleted = true,
                canEditEntities = true,
            )

        assertFalse(vmState.toUiState().canInvokeDeletion)
    }

    @Test
    fun `canInvokeDeletion is false when project is closed`() {
        val vmState =
            ProjectDetailsVmState(
                projectStatus = ProjectDetailsUiStatus.LOADED,
                project = closedProject(),
                isBeingDeleted = false,
                canEditEntities = true,
            )

        assertFalse(vmState.toUiState().canInvokeDeletion)
    }

    @Test
    fun `canInvokeDeletion is false when canEditEntities is false`() {
        val vmState =
            ProjectDetailsVmState(
                projectStatus = ProjectDetailsUiStatus.LOADED,
                project = openProject(),
                isBeingDeleted = false,
                canEditEntities = false,
            )

        assertFalse(vmState.toUiState().canInvokeDeletion)
    }

    @Test
    fun `canDownloadReport is true when LOADED and not downloading`() {
        val vmState =
            ProjectDetailsVmState(
                projectStatus = ProjectDetailsUiStatus.LOADED,
                isDownloadingReport = false,
            )

        assertTrue(vmState.toUiState().canDownloadReport)
    }

    @Test
    fun `canDownloadReport is false when downloading`() {
        val vmState =
            ProjectDetailsVmState(
                projectStatus = ProjectDetailsUiStatus.LOADED,
                isDownloadingReport = true,
            )

        assertFalse(vmState.toUiState().canDownloadReport)
    }

    @Test
    fun `canDownloadReport is false when not LOADED`() {
        val vmState =
            ProjectDetailsVmState(
                projectStatus = ProjectDetailsUiStatus.LOADING,
                isDownloadingReport = false,
            )

        assertFalse(vmState.toUiState().canDownloadReport)
    }

    @Test
    fun `canToggleClosed is true when LOADED and not closing or reopening and canCloseProject is true`() {
        val vmState =
            ProjectDetailsVmState(
                projectStatus = ProjectDetailsUiStatus.LOADED,
                isClosingOrReopening = false,
                canCloseProject = true,
            )

        assertTrue(vmState.toUiState().canToggleClosed)
    }

    @Test
    fun `canToggleClosed is false when closing or reopening`() {
        val vmState =
            ProjectDetailsVmState(
                projectStatus = ProjectDetailsUiStatus.LOADED,
                isClosingOrReopening = true,
                canCloseProject = true,
            )

        assertFalse(vmState.toUiState().canToggleClosed)
    }

    @Test
    fun `canToggleClosed is false when not LOADED`() {
        val vmState =
            ProjectDetailsVmState(
                projectStatus = ProjectDetailsUiStatus.ERROR,
                isClosingOrReopening = false,
                canCloseProject = true,
            )

        assertFalse(vmState.toUiState().canToggleClosed)
    }

    @Test
    fun `canToggleClosed is false when canCloseProject is false`() {
        val vmState =
            ProjectDetailsVmState(
                projectStatus = ProjectDetailsUiStatus.LOADED,
                isClosingOrReopening = false,
                canCloseProject = false,
            )

        assertFalse(vmState.toUiState().canToggleClosed)
    }

    @Test
    fun `creatorEmail is resolved from allUsers matching project creatorId`() {
        val creatorId = UuidProvider.getUuid()
        val vmState =
            ProjectDetailsVmState(
                project = AppProjectData(
                    id = Uuid.random(),
                    name = "Test",
                    address = "Address",
                    creatorId = creatorId,
                    updatedAt = Timestamp(0L),
                    isClosed = false,
                ),
                allUsers = persistentListOf(
                    AppUserData(
                        id = creatorId,
                        authProviderId = "auth-1",
                        email = "creator@example.com",
                        role = UserRole.SERVICE_WORKER,
                        updatedAt = Timestamp(0L),
                    ),
                ),
            )

        assertEquals("creator@example.com", vmState.toUiState().creatorEmail)
    }

    @Test
    fun `creatorEmail is null when creator not in allUsers`() {
        val vmState =
            ProjectDetailsVmState(
                project = openProject(),
                allUsers = persistentListOf(
                    AppUserData(
                        id = UuidProvider.getUuid(),
                        authProviderId = "auth-1",
                        email = "other@example.com",
                        role = UserRole.PASSPORT_LEAD,
                        updatedAt = Timestamp(0L),
                    ),
                ),
            )

        assertNull(vmState.toUiState().creatorEmail)
    }

    @Test
    fun `creatorEmail is null when project is null`() {
        val vmState = ProjectDetailsVmState(project = null)

        assertNull(vmState.toUiState().creatorEmail)
    }
}
