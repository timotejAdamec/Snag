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

package cz.adamec.timotej.snag.structures.fe.driving.impl.internal.structureDetails.vm

import cz.adamec.timotej.snag.feat.structures.business.Structure

internal data class StructureDetailsUiState(
    val status: StructureDetailsUiStatus = StructureDetailsUiStatus.LOADING,
    val isBeingDeleted: Boolean = false,
    val structure: Structure? = null,
) {
    val canInvokeDeletion = status == StructureDetailsUiStatus.LOADED && !isBeingDeleted
}

internal enum class StructureDetailsUiStatus {
    ERROR,
    NOT_FOUND,
    LOADING,
    LOADED,
    DELETED,
}
