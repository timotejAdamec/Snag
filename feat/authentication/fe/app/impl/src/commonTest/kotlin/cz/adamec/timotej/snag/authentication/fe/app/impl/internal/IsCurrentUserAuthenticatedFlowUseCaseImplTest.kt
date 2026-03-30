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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IsCurrentUserAuthenticatedFlowUseCaseImplTest {
    @Test
    fun `returns true when authenticated`() =
        runTest {
            val provider =
                FakeAuthTokenProvider(
                    initialState = AuthState.Authenticated(authProviderId = "test-id"),
                )
            val useCase = IsCurrentUserAuthenticatedFlowUseCaseImpl(authTokenProvider = provider)

            assertTrue(useCase().first())
        }

    @Test
    fun `returns false when unauthenticated`() =
        runTest {
            val provider = FakeAuthTokenProvider(initialState = AuthState.Unauthenticated)
            val useCase = IsCurrentUserAuthenticatedFlowUseCaseImpl(authTokenProvider = provider)

            assertFalse(useCase().first())
        }

    @Test
    fun `returns false when loading`() =
        runTest {
            val provider = FakeAuthTokenProvider(initialState = AuthState.Loading)
            val useCase = IsCurrentUserAuthenticatedFlowUseCaseImpl(authTokenProvider = provider)

            assertFalse(useCase().first())
        }
}
