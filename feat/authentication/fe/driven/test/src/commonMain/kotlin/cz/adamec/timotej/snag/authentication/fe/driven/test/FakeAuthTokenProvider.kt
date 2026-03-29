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
import kotlin.uuid.Uuid

private val DEFAULT_USER_UUID = Uuid.parse("00000000-0000-0000-0005-000000000001")

class FakeAuthTokenProvider(
    initialState: AuthState = AuthState.Authenticated(userId = DEFAULT_USER_UUID),
) : AuthTokenProvider {
    private val _authState = MutableStateFlow(initialState)
    override val authState: StateFlow<AuthState> = _authState

    override suspend fun login() {
        _authState.value = AuthState.Authenticated(userId = DEFAULT_USER_UUID)
    }

    override fun setAuthenticatedUserId(userId: Uuid) {
        _authState.value = AuthState.Authenticated(userId = userId)
    }

    override suspend fun getAccessToken(): String? = "fake-access-token"

    override suspend fun logout() {
        _authState.value = AuthState.Unauthenticated
    }
}
