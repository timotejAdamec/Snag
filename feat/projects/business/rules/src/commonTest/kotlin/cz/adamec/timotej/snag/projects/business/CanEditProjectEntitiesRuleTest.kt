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

import cz.adamec.timotej.snag.core.foundation.common.UuidProvider
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class CanEditProjectEntitiesRuleTest {
    private val rule = CanEditProjectEntitiesRule()

    private fun createProject(isClosed: Boolean) =
        object : Project {
            override val id: Uuid = UuidProvider.getUuid()
            override val name: String = "Test Project"
            override val address: String = "Test Address"
            override val clientId: Uuid? = null
            override val isClosed: Boolean = isClosed
        }

    @Test
    fun `returns true for open project`() {
        assertTrue(rule(createProject(isClosed = false)))
    }

    @Test
    fun `returns false for closed project`() {
        assertFalse(rule(createProject(isClosed = true)))
    }
}
