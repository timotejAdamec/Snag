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
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GetAuthProviderIdUseCaseImplTest {
    @Test
    fun `first emission is authProviderId when session exists`() =
        runTest {
            val provider =
                FakeAuthTokenProvider(
                    initialState = AuthState.Authenticated(authProviderId = "existing-session-id"),
                )
            val useCase = GetAuthProviderIdUseCaseImpl(authTokenProvider = provider)

            val firstValue = useCase().first()

            assertEquals(expected = "existing-session-id", actual = firstValue)
        }

    @Test
    fun `first emission is null when unauthenticated`() =
        runTest {
            val provider = FakeAuthTokenProvider(initialState = AuthState.Unauthenticated)
            val useCase = GetAuthProviderIdUseCaseImpl(authTokenProvider = provider)

            val firstValue = useCase().first()

            assertNull(firstValue)
        }
}
