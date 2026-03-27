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

import cz.adamec.timotej.snag.feat.inspections.app.model.AppInspection
import cz.adamec.timotej.snag.feat.structures.app.model.AppStructure
import cz.adamec.timotej.snag.projects.app.model.AppProject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal data class ProjectDetailsUiState(
    val projectStatus: ProjectDetailsUiStatus = ProjectDetailsUiStatus.LOADING,
    val structureStatus: StructuresUiStatus = StructuresUiStatus.LOADED,
    val inspectionStatus: InspectionsUiStatus = InspectionsUiStatus.LOADED,
    val isBeingDeleted: Boolean = false,
    val isDownloadingReport: Boolean = false,
    val isClosingOrReopening: Boolean = false,
    val project: AppProject? = null,
    val structures: ImmutableList<AppStructure> = persistentListOf(),
    val inspections: ImmutableList<AppInspection> = persistentListOf(),
    val canEditEntities: Boolean = false,
    val canCloseProject: Boolean = false,
) {
    val isClosed: Boolean get() = project?.isClosed == true
    val isProjectEditable: Boolean get() = projectStatus == ProjectDetailsUiStatus.LOADED && !isClosed && canEditEntities
    val canInvokeDeletion = isProjectEditable && !isBeingDeleted
    val canDownloadReport = projectStatus == ProjectDetailsUiStatus.LOADED && !isDownloadingReport
    val canToggleClosed = projectStatus == ProjectDetailsUiStatus.LOADED && !isClosingOrReopening && canCloseProject
}

internal enum class ProjectDetailsUiStatus {
    ERROR,
    NOT_FOUND,
    LOADING,
    LOADED,
    DELETED,
}

internal enum class StructuresUiStatus {
    ERROR,
    LOADED,
}

internal enum class InspectionsUiStatus {
    ERROR,
    LOADED,
}
