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

package cz.adamec.timotej.snag.authentication.fe.driven.test

import cz.adamec.timotej.snag.authentication.fe.ports.AuthState
import cz.adamec.timotej.snag.authentication.fe.ports.AuthTokenProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeAuthTokenProvider(
    initialState: AuthState = AuthState.Authenticated(authProviderId = "fake-provider-id"),
) : AuthTokenProvider {
    private val _authState = MutableStateFlow(initialState)
    override val authState: StateFlow<AuthState> = _authState

    var loginFailure: Throwable? = null

    override suspend fun restoreSession() = Unit

    override suspend fun login() {
        loginFailure?.let { throw it }
        _authState.value = AuthState.Authenticated(authProviderId = "fake-provider-id")
    }

    override suspend fun getAccessToken(): String? = "fake-access-token"

    override suspend fun logout() {
        _authState.value = AuthState.Unauthenticated
    }
}
