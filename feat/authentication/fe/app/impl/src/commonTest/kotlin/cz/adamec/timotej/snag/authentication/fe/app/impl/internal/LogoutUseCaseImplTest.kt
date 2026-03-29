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

class LogoutUseCaseImplTest {
    @Test
    fun `sets state to unauthenticated after logout`() =
        runTest {
            val provider = FakeAuthTokenProvider()
            val useCase = LogoutUseCaseImpl(authTokenProvider = provider)

            useCase()

            assertIs<AuthState.Unauthenticated>(provider.authState.value)
        }
}
