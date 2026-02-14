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

package cz.adamec.timotej.snag.feat.inspections.business

import cz.adamec.timotej.snag.lib.core.common.Timestamp
import kotlin.uuid.Uuid

data class Inspection(
    val id: Uuid,
    val projectId: Uuid,
    val startedAt: Timestamp?,
    val endedAt: Timestamp?,
    val participants: String?,
    val climate: String?,
    val note: String?,
    val updatedAt: Timestamp,
)
