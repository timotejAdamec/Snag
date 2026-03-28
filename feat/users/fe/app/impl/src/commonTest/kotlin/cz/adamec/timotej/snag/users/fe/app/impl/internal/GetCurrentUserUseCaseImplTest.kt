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

import cz.adamec.timotej.snag.core.foundation.fe.CurrentUserIdStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.uuid.Uuid

class GetCurrentUserUseCaseImplTest {
    private val currentUserIdStore = CurrentUserIdStore()
    private val useCase = GetCurrentUserUseCaseImpl(currentUserIdStore)

    @Test
    fun `returns current user id when set`() {
        val expectedId = Uuid.parse("00000000-0000-0000-0000-000000000001")
        currentUserIdStore.set(expectedId)

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
