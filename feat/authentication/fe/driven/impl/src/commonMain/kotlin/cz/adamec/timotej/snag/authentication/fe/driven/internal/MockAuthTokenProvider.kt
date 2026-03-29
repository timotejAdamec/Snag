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
import kotlin.uuid.Uuid

internal class MockAuthTokenProvider : AuthTokenProvider {
    private val _authState =
        MutableStateFlow<AuthState>(
            AuthState.Authenticated(userId = MOCK_USER_UUID),
        )
    override val authState: StateFlow<AuthState> = _authState

    override suspend fun login() {
        _authState.value = AuthState.Authenticated(userId = MOCK_USER_UUID)
    }

    override fun setAuthenticatedUserId(userId: Uuid) {
        _authState.value = AuthState.Authenticated(userId = userId)
    }

    override suspend fun getAccessToken(): String? = MOCK_USER_ID

    override suspend fun logout() {
        _authState.value = AuthState.Unauthenticated
    }

    internal companion object {
        const val MOCK_USER_ID = "00000000-0000-0000-0005-000000000001"
        val MOCK_USER_UUID: Uuid = Uuid.parse(MOCK_USER_ID)
    }
}
