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

package cz.adamec.timotej.snag.buildsrc.configuration.architecture

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CheckDependencyCombinerTest {

    @Test
    fun `valid dependency produces no violations`() {
        val source = parseModulePath(":feat:projects:fe:driving:impl")
        val target = parseModulePath(":feat:projects:fe:app:api")
        assertTrue(checkDependency(source, target).isEmpty())
    }

    @Test
    fun `core depending on feat produces category violation`() {
        val source = parseModulePath(":core:foundation:common")
        val target = parseModulePath(":feat:projects:business:model")
        val violations = checkDependency(source, target)
        assertEquals(1, violations.size)
        assertEquals(RuleId.CATEGORY_DIRECTION, violations.first().ruleId)
    }

    @Test
    fun `multiple violations can be produced`() {
        // app:api depending on driving:impl violates both hexagonal and encapsulation rules
        val source = parseModulePath(":feat:projects:fe:app:api")
        val target = parseModulePath(":feat:projects:fe:driving:impl")
        val violations = checkDependency(source, target)
        assertTrue(violations.size >= 2)
        assertTrue(violations.any { it.ruleId == RuleId.HEXAGONAL_DIRECTION })
        assertTrue(violations.any { it.ruleId == RuleId.ENCAPSULATION_DIRECTION })
    }
}
