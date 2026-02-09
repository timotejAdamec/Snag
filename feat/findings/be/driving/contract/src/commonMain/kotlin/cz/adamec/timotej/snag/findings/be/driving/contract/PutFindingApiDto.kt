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

package cz.adamec.timotej.snag.findings.be.driving.contract

import cz.adamec.timotej.snag.lib.core.common.Timestamp
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class PutFindingApiDto(
    val structureId: Uuid,
    val type: String,
    val name: String,
    val description: String?,
    val importance: String? = null,
    val term: String? = null,
    val coordinates: List<RelativeCoordinateApiDto>,
    val updatedAt: Timestamp,
)
