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

package cz.adamec.timotej.snag.users.contract

import kotlinx.serialization.Serializable

@Serializable
data class UserApiDto(
    val id: String,
    val authProviderId: String,
    val email: String,
    val role: String? = null,
    val updatedAt: Long,
)
