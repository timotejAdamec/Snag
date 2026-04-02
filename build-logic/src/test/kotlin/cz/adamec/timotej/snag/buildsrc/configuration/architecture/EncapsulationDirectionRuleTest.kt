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
import kotlin.test.assertNull

class EncapsulationDirectionRuleTest {

    @Test
    fun `impl depending on api is allowed`() {
        val source = parseModulePath(":feat:projects:fe:app:impl")
        val target = parseModulePath(":feat:projects:fe:app:api")
        assertNull(checkEncapsulationDirection(source, target))
    }

    @Test
    fun `test depending on api is allowed`() {
        val source = parseModulePath(":lib:network:fe:test")
        val target = parseModulePath(":lib:network:fe:api")
        assertNull(checkEncapsulationDirection(source, target))
    }

    @Test
    fun `test depending on impl is allowed`() {
        val source = parseModulePath(":feat:projects:fe:driven:test")
        val target = parseModulePath(":feat:projects:fe:driven:impl")
        assertNull(checkEncapsulationDirection(source, target))
    }

    @Test
    fun `contract depending on api is allowed`() {
        val source = parseModulePath(":lib:storage:contract")
        val target = parseModulePath(":lib:configuration:common:api")
        assertNull(checkEncapsulationDirection(source, target))
    }

    @Test
    fun `api depending on impl is a violation`() {
        val source = parseModulePath(":feat:projects:fe:app:api")
        val target = parseModulePath(":feat:projects:fe:app:impl")
        val violation = checkEncapsulationDirection(source, target)
        assertEquals(RuleId.ENCAPSULATION_DIRECTION, violation?.ruleId)
    }

    @Test
    fun `api depending on test is a violation`() {
        val source = parseModulePath(":lib:network:fe:api")
        val target = parseModulePath(":lib:network:fe:test")
        val violation = checkEncapsulationDirection(source, target)
        assertEquals(RuleId.ENCAPSULATION_DIRECTION, violation?.ruleId)
    }

    @Test
    fun `impl depending on test is a violation`() {
        val source = parseModulePath(":feat:projects:fe:driven:impl")
        val target = parseModulePath(":feat:projects:fe:driven:test")
        val violation = checkEncapsulationDirection(source, target)
        assertEquals(RuleId.ENCAPSULATION_DIRECTION, violation?.ruleId)
    }

    @Test
    fun `modules without encapsulation - rule does not apply`() {
        val source = parseModulePath(":feat:projects:fe:ports")
        val target = parseModulePath(":feat:projects:business:model")
        assertNull(checkEncapsulationDirection(source, target))
    }
}
