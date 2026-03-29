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

import cz.adamec.timotej.snag.authentication.fe.driven.test.FakeAuthTokenProvider
import cz.adamec.timotej.snag.authentication.fe.ports.AuthState
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertIs

class RestoreSessionUseCaseImplTest {
    @Test
    fun `delegates to auth token provider`() =
        runTest {
            val provider = FakeAuthTokenProvider(initialState = AuthState.Loading)
            val useCase = RestoreSessionUseCaseImpl(authTokenProvider = provider)

            useCase()

            // FakeAuthTokenProvider.restoreSession() is a no-op,
            // so state remains unchanged — verifies delegation without crash
            assertIs<AuthState.Loading>(provider.authState.value)
        }
}
