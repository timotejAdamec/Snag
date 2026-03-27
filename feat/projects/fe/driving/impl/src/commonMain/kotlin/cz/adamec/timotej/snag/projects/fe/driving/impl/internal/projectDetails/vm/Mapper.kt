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

internal fun ProjectDetailsVmState.toUiState(): ProjectDetailsUiState {
    val isClosed = project?.isClosed == true
    val isProjectEditable = projectStatus == ProjectDetailsUiStatus.LOADED && !isClosed
    return ProjectDetailsUiState(
        projectStatus = projectStatus,
        isDownloadingReport = isDownloadingReport,
        isClosingOrReopening = isClosingOrReopening,
        project = project,
        structures = structures,
        inspections = inspections,
        isClosed = isClosed,
        isProjectEditable = isProjectEditable,
        canInvokeDeletion = isProjectEditable && !isBeingDeleted,
        canDownloadReport = projectStatus == ProjectDetailsUiStatus.LOADED && !isDownloadingReport,
        canToggleClosed = projectStatus == ProjectDetailsUiStatus.LOADED && !isClosingOrReopening,
    )
}
