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

package cz.adamec.timotej.snag.authentication.fe.driven.internal

import cz.adamec.timotej.snag.authentication.fe.ports.AuthState
import cz.adamec.timotej.snag.authentication.fe.ports.AuthTokenProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class MockAuthTokenProvider : AuthTokenProvider {
    private val _authState =
        MutableStateFlow<AuthState>(
            AuthState.Authenticated(authProviderId = MOCK_AUTH_PROVIDER_ID),
        )
    override val authState: StateFlow<AuthState> = _authState

    override suspend fun restoreSession() = Unit

    override suspend fun login() {
        _authState.value = AuthState.Authenticated(authProviderId = MOCK_AUTH_PROVIDER_ID)
    }

    override suspend fun getAccessToken(): String? = MOCK_AUTH_PROVIDER_ID

    override suspend fun logout() {
        _authState.value = AuthState.Unauthenticated
    }

    internal companion object {
        const val MOCK_AUTH_PROVIDER_ID = "mock-auth-provider-id"
    }
}
