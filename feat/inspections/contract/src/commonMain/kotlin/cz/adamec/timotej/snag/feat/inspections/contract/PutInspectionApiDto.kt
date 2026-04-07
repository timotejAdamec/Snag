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

package cz.adamec.timotej.snag.feat.inspections.contract

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class PutInspectionApiDto(
    val projectId: Uuid,
    val dateFrom: Timestamp?,
    val dateTo: Timestamp?,
    val participants: String?,
    val climate: String?,
    val note: String?,
    val updatedAt: Timestamp,
)
