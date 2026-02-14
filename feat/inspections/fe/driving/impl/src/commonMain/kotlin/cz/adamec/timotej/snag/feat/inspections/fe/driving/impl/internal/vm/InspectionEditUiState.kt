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

package cz.adamec.timotej.snag.feat.inspections.fe.driving.impl.internal.vm

import kotlin.uuid.Uuid

internal data class InspectionEditUiState(
    val projectId: Uuid? = null,
    val startedAt: String = "",
    val endedAt: String = "",
    val participants: String = "",
    val climate: String = "",
    val note: String = "",
)
