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

package cz.adamec.timotej.snag.findings.fe.driving.impl.internal.findingDetail.vm

internal fun FindingDetailVmState.toUiState(): FindingDetailUiState =
    FindingDetailUiState(
        status = status,
        finding = finding,
        canEdit = status == FindingDetailUiStatus.LOADED && !isBeingDeleted && canEditFinding,
        photos = photos,
        isAddingPhoto = isAddingPhoto,
    )
