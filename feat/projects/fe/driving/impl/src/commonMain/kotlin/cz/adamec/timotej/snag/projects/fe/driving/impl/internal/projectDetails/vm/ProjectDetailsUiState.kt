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

import FrontendStructure
import cz.adamec.timotej.snag.feat.structures.business.Structure
import cz.adamec.timotej.snag.projects.business.Project
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal data class ProjectDetailsUiState(
    val projectStatus: ProjectDetailsUiStatus = ProjectDetailsUiStatus.LOADING,
    val structureStatus: StructuresUiStatus = StructuresUiStatus.LOADING,
    val isBeingDeleted: Boolean = false,
    val project: Project? = null,
    val structures: ImmutableList<FrontendStructure> = persistentListOf(),
) {
    val canInvokeDeletion = projectStatus == ProjectDetailsUiStatus.LOADED && !isBeingDeleted
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
    LOADING,
    LOADED,
}
