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

package cz.adamec.timotej.snag.users.fe.app.impl.internal

import cz.adamec.timotej.snag.authentication.fe.app.api.AuthenticatedUserProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.uuid.Uuid

class GetCurrentUserUseCaseImplTest {
    private val fakeProvider = FakeAuthenticatedUserProvider()
    private val useCase = GetCurrentUserUseCaseImpl(fakeProvider)

    @Test
    fun `returns current user id when set`() {
        val expectedId = Uuid.parse("00000000-0000-0000-0000-000000000001")
        fakeProvider.setUserId(expectedId)

        val result = useCase()

        assertEquals(expectedId, result)
    }

    @Test
    fun `throws when current user id not set`() {
        assertFailsWith<IllegalStateException> {
            useCase()
        }
    }
}

private class FakeAuthenticatedUserProvider : AuthenticatedUserProvider {
    private val _currentUserId = MutableStateFlow<Uuid?>(null)
    override val currentUserId: StateFlow<Uuid?> = _currentUserId

    fun setUserId(userId: Uuid) {
        _currentUserId.value = userId
    }

    override fun requireCurrentUserId(): Uuid = _currentUserId.value ?: error("Not set")
}
