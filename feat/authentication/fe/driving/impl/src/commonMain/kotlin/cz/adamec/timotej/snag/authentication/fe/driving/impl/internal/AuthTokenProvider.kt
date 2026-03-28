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

package cz.adamec.timotej.snag.authentication.fe.driving.impl.internal

import kotlinx.coroutines.flow.StateFlow

internal interface AuthTokenProvider {
    val authState: StateFlow<AuthState>

    suspend fun getAccessToken(): String?

    suspend fun signOut()
}

internal sealed interface AuthState {
    data object Unauthenticated : AuthState

    data object Authenticated : AuthState
}
