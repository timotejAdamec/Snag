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
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetAccessTokenUseCaseImplTest {
    @Test
    fun `returns access token from provider`() =
        runTest {
            val provider = FakeAuthTokenProvider()
            val useCase = GetAccessTokenUseCaseImpl(authTokenProvider = provider)

            assertEquals(expected = "fake-access-token", actual = useCase())
        }
}
