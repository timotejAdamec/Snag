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

import cz.adamec.timotej.snag.authentication.fe.driven.internal.LH.logger
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

    override suspend fun restoreSession() {
        logger.d { "Mock: restoring session (no-op, already authenticated)." }
    }

    override suspend fun login() {
        logger.d { "Mock: logging in with authProviderId=$MOCK_AUTH_PROVIDER_ID." }
        _authState.value = AuthState.Authenticated(authProviderId = MOCK_AUTH_PROVIDER_ID)
    }

    override suspend fun getAccessToken(): String? {
        logger.d { "Mock: returning mock user ID as access token." }
        return MOCK_USER_ID
    }

    override suspend fun refreshAccessToken(): String? {
        logger.d { "Mock: returning mock user ID as refreshed access token." }
        return MOCK_USER_ID
    }

    override suspend fun logout() {
        logger.d { "Mock: logging out." }
        _authState.value = AuthState.Unauthenticated
    }

    internal companion object {
        const val MOCK_AUTH_PROVIDER_ID = "mock-auth-provider-id"
        private const val MOCK_USER_ID = "00000000-0000-0000-0005-000000000001"
    }
}
