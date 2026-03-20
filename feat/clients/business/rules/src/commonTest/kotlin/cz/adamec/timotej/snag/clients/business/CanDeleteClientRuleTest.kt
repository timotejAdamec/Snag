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

package cz.adamec.timotej.snag.clients.business

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CanDeleteClientRuleTest {
    private val rule = CanDeleteClientRule()

    @Test
    fun `returns true when client is not referenced by any project`() {
        assertTrue(rule(isReferencedByProject = false))
    }

    @Test
    fun `returns false when client is referenced by a project`() {
        assertFalse(rule(isReferencedByProject = true))
    }
}
