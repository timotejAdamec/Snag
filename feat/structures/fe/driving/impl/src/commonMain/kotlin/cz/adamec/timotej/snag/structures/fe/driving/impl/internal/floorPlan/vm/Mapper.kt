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

internal fun StructureDetailsVmState.toUiState(): StructureDetailsUiState =
    StructureDetailsUiState(
        status = status,
        feStructure = feStructure,
        findings = findings,
        selectedFindingId = selectedFindingId,
        canEdit = status == StructureDetailsUiStatus.LOADED && !isBeingDeleted && canEditStructure,
    )
