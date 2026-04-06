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
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projectAssignments.vm.AssignedUserItem
import cz.adamec.timotej.snag.reports.business.ReportType
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal data class ProjectDetailsUiState(
    val projectStatus: ProjectDetailsUiStatus = ProjectDetailsUiStatus.LOADING,
    val isDownloadingReport: Boolean = false,
    val isClosingOrReopening: Boolean = false,
    val project: AppProject? = null,
    val structures: ImmutableList<AppStructure> = persistentListOf(),
    val inspections: ImmutableList<AppInspection> = persistentListOf(),
    val assignedUsers: ImmutableList<AssignedUserItem> = persistentListOf(),
    val availableUsers: ImmutableList<AssignedUserItem> = persistentListOf(),
    val isClosed: Boolean = false,
    val isProjectEditable: Boolean = false,
    val canInvokeDeletion: Boolean = false,
    val canDownloadReport: Boolean = false,
    val availableReportTypes: List<ReportType> = emptyList(),
    val canToggleClosed: Boolean = false,
    val canAssignUsers: Boolean = false,
    val creatorEmail: String? = null,
)

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
