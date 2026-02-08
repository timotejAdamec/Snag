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

package cz.adamec.timotej.snag.clients.be.driving.contract

import cz.adamec.timotej.snag.lib.core.common.Timestamp
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class ClientApiDto(
    val id: Uuid,
    val name: String,
    val address: String? = null,
    val phoneNumber: String? = null,
    val email: String? = null,
    val updatedAt: Timestamp,
    val deletedAt: Timestamp? = null,
)
