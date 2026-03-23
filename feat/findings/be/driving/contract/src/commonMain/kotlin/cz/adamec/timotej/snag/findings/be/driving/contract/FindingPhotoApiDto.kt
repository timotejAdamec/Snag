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

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class FindingPhotoApiDto(
    val id: Uuid,
    val findingId: Uuid,
    val url: String,
    val updatedAt: Timestamp,
    val deletedAt: Timestamp? = null,
)

@Serializable
data class PutFindingPhotoApiDto(
    val findingId: Uuid,
    val url: String,
    val updatedAt: Timestamp,
)

@Serializable
data class DeleteFindingPhotoApiDto(
    val deletedAt: Timestamp,
)
