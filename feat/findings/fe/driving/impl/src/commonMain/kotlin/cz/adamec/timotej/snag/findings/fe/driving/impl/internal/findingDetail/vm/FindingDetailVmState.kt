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

import cz.adamec.timotej.snag.feat.findings.app.model.AppFinding
import cz.adamec.timotej.snag.feat.findings.app.model.AppFindingPhoto

internal data class FindingDetailVmState(
    val status: FindingDetailUiStatus = FindingDetailUiStatus.LOADING,
    val finding: AppFinding? = null,
    val isBeingDeleted: Boolean = false,
    val canEditFinding: Boolean = true,
    val photos: List<AppFindingPhoto> = emptyList(),
    val isAddingPhoto: Boolean = false,
    val canModifyPhotos: Boolean = true,
)
