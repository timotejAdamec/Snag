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

package cz.adamec.timotej.snag.authentication.fe.app.impl.internal

import cz.adamec.timotej.snag.authentication.fe.app.api.LoginResult
import cz.adamec.timotej.snag.authentication.fe.driven.test.FakeAuthTokenProvider
import cz.adamec.timotej.snag.authentication.fe.ports.AuthState
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertIs

class LoginUseCaseImplTest {
    @Test
    fun `returns success on successful login`() =
        runTest {
            val provider = FakeAuthTokenProvider(initialState = AuthState.Unauthenticated)
            val useCase = LoginUseCaseImpl(authTokenProvider = provider)

            val result = useCase()

            assertIs<LoginResult.Success>(result)
        }

    @Test
    fun `returns error when login fails`() =
        runTest {
            val provider = FakeAuthTokenProvider(initialState = AuthState.Unauthenticated)
            provider.loginFailure = IllegalStateException("Network error")
            val useCase = LoginUseCaseImpl(authTokenProvider = provider)

            val result = useCase()

            assertIs<LoginResult.Error>(result)
        }
}
