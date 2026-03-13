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

package cz.adamec.timotej.snag.projects.business

import cz.adamec.timotej.snag.lib.core.common.Timestamp
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class CanEditProjectEntitiesRuleTest {
    private val rule = CanEditProjectEntitiesRule()

    private fun createProject(isClosed: Boolean) =
        Project(
            id = Uuid.parse("00000000-0000-0000-0000-000000000001"),
            name = "Test Project",
            address = "Test Address",
            isClosed = isClosed,
            updatedAt = Timestamp(1L),
        )

    @Test
    fun `returns true for open project`() {
        assertTrue(rule(createProject(isClosed = false)))
    }

    @Test
    fun `returns false for closed project`() {
        assertFalse(rule(createProject(isClosed = true)))
    }
}
