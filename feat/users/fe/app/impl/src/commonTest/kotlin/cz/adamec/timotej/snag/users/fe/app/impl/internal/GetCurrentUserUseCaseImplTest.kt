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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

class GetCurrentUserUseCaseImplTest {
    private val useCase = GetCurrentUserUseCaseImpl()

    @Test
    fun `returns hardcoded user id`() {
        val result = useCase()
        assertEquals(Uuid.parse("00000000-0000-0000-0000-000000000001"), result)
    }
}
