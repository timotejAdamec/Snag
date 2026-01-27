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

internal data class ProjectDetailsUiState(
    val status: ProjectDetailsUiStatus = ProjectDetailsUiStatus.LOADING,
    val isBeingDeleted: Boolean = false,
    val name: String = "",
    val address: String = "",
) {
    val canInvokeDeletion = status == ProjectDetailsUiStatus.LOADED && !isBeingDeleted
}

internal enum class ProjectDetailsUiStatus {
    NOT_FOUND,
    LOADING,
    LOADED,
    DELETED,
}
