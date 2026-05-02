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
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class RefreshAccessTokenUseCaseImplTest {
    @Test
    fun `returns refreshed token from provider`() =
        runTest {
            val provider = FakeAuthTokenProvider()
            val useCase = RefreshAccessTokenUseCaseImpl(authTokenProvider = provider)

            assertEquals(expected = "fake-access-token", actual = useCase())
        }

    @Test
    fun `triggers logout and returns null on provider failure`() =
        runTest {
            val provider = FakeAuthTokenProvider()
            provider.refreshFailure = IllegalStateException("Refresh failed")
            val useCase = RefreshAccessTokenUseCaseImpl(authTokenProvider = provider)

            val result = useCase()

            assertNull(actual = result)
            assertIs<AuthState.Unauthenticated>(provider.authState.value)
        }
}
