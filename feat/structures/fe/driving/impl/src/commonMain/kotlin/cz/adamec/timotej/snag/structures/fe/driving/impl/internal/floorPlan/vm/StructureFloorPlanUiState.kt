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

package cz.adamec.timotej.snag.structures.fe.driving.impl.internal.floorPlan.vm

import FrontendStructure
import androidx.compose.runtime.Immutable
import cz.adamec.timotej.snag.feat.findings.business.Finding
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlin.uuid.Uuid

@Immutable
internal data class StructureDetailsUiState(
    val status: StructureDetailsUiStatus = StructureDetailsUiStatus.LOADING,
    val isBeingDeleted: Boolean = false,
    val feStructure: FrontendStructure? = null,
    val findings: ImmutableList<Finding> = persistentListOf(),
    val selectedFindingId: Uuid? = null,
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
