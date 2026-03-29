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

package cz.adamec.timotej.snag.authentication.fe.ports

import kotlinx.coroutines.flow.StateFlow
import kotlin.uuid.Uuid

interface AuthTokenProvider {
    val authState: StateFlow<AuthState>

    suspend fun login()

    fun setAuthenticatedUserId(userId: Uuid)

    suspend fun getAccessToken(): String?

    suspend fun logout()
}
