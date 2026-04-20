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

package cz.adamec.timotej.snag.wear.seed

import cz.adamec.timotej.snag.authentication.fe.ports.AuthState
import cz.adamec.timotej.snag.authentication.fe.ports.AuthTokenProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class FakeAuthTokenProvider : AuthTokenProvider {
    override val authState: StateFlow<AuthState> =
        MutableStateFlow(AuthState.Authenticated(authProviderId = "seed-user"))

    override suspend fun restoreSession() = Unit

    override suspend fun login() = Unit

    override suspend fun getAccessToken(): String? = "seed-access-token"

    override suspend fun logout() = Unit
}
