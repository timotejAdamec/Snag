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

import cz.adamec.timotej.snag.feat.findings.fe.model.FrontendFinding

internal data class FindingDetailUiState(
    val status: FindingDetailUiStatus = FindingDetailUiStatus.LOADING,
    val finding: FrontendFinding? = null,
    val isBeingDeleted: Boolean = false,
) {
    val canInvokeDeletion: Boolean
        get() = status == FindingDetailUiStatus.LOADED && !isBeingDeleted
}

internal enum class FindingDetailUiStatus {
    ERROR,
    NOT_FOUND,
    LOADING,
    LOADED,
    DELETED,
}
