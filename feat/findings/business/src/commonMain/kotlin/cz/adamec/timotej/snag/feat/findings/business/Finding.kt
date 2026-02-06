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

package cz.adamec.timotej.snag.feat.findings.business

import cz.adamec.timotej.snag.lib.core.common.Timestamp
import kotlin.uuid.Uuid

data class Finding(
    val id: Uuid,
    val structureId: Uuid,
    val name: String,
    val description: String?,
    val coordinates: List<RelativeCoordinate>,
    val updatedAt: Timestamp,
)
