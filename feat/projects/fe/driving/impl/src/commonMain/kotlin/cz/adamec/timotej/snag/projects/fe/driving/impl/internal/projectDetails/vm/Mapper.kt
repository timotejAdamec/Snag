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

import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projectAssignments.vm.toAssignedUserItem
import kotlinx.collections.immutable.toPersistentList

internal fun ProjectDetailsVmState.toUiState(): ProjectDetailsUiState {
    val isClosed = project?.isClosed == true
    val isProjectEditable = projectStatus == ProjectDetailsUiStatus.LOADED && !isClosed && canEditEntities
    val assignedUsers =
        allUsers
            .filter { it.id in assignedUserIds }
            .map { it.toAssignedUserItem() }
            .toPersistentList()
    val availableUsers =
        allUsers
            .filter { it.id !in assignedUserIds }
            .map { it.toAssignedUserItem() }
            .toPersistentList()
    val creatorEmail = allUsers.find { it.id == project?.creatorId }?.email
    return ProjectDetailsUiState(
        projectStatus = projectStatus,
        isDownloadingReport = isDownloadingReport,
        isClosingOrReopening = isClosingOrReopening,
        project = project,
        structures = structures,
        inspections = inspections,
        assignedUsers = assignedUsers,
        availableUsers = availableUsers,
        isClosed = isClosed,
        isProjectEditable = isProjectEditable,
        canInvokeDeletion = isProjectEditable && !isBeingDeleted,
        canDownloadReport = projectStatus == ProjectDetailsUiStatus.LOADED && !isDownloadingReport,
        canToggleClosed = projectStatus == ProjectDetailsUiStatus.LOADED && !isClosingOrReopening && canCloseProject,
        canAssignUsers = canAssignUsers,
        creatorEmail = creatorEmail,
    )
}
