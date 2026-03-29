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

sealed interface AuthState {
    data object Loading : AuthState

    data object Unauthenticated : AuthState

    data class Authenticated(
        val authProviderId: String,
    ) : AuthState
}
