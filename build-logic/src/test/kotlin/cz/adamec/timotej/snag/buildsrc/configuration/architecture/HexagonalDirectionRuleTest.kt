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

class HexagonalDirectionRuleTest {

    // --- Same feature: allowed directions ---

    @Test
    fun `driving depending on app is allowed`() {
        val source = parseModulePath(":feat:projects:fe:driving:impl")
        val target = parseModulePath(":feat:projects:fe:app:api")
        assertNull(checkHexagonalDirection(source, target))
    }

    @Test
    fun `driven depending on app is allowed`() {
        val source = parseModulePath(":feat:projects:fe:driven:impl")
        val target = parseModulePath(":feat:projects:fe:app:api")
        assertNull(checkHexagonalDirection(source, target))
    }

    @Test
    fun `app depending on ports is allowed`() {
        val source = parseModulePath(":feat:projects:fe:app:impl")
        val target = parseModulePath(":feat:projects:fe:ports")
        assertNull(checkHexagonalDirection(source, target))
    }

    @Test
    fun `driven depending on ports is allowed`() {
        val source = parseModulePath(":feat:projects:fe:driven:impl")
        val target = parseModulePath(":feat:projects:fe:ports")
        assertNull(checkHexagonalDirection(source, target))
    }

    @Test
    fun `app depending on business model is allowed`() {
        val source = parseModulePath(":feat:projects:fe:app:impl")
        val target = parseModulePath(":feat:projects:business:model")
        assertNull(checkHexagonalDirection(source, target))
    }

    @Test
    fun `ports depending on app model is allowed - models are data containers`() {
        val source = parseModulePath(":feat:projects:be:ports")
        val target = parseModulePath(":feat:projects:be:app:model")
        assertNull(checkHexagonalDirection(source, target))
    }

    @Test
    fun `business depending on app model is allowed`() {
        val source = parseModulePath(":feat:projects:business:model")
        val target = parseModulePath(":feat:projects:app:model")
        assertNull(checkHexagonalDirection(source, target))
    }

    @Test
    fun `business depending on business is allowed`() {
        val source = parseModulePath(":feat:projects:business:rules")
        val target = parseModulePath(":feat:projects:business:model")
        assertNull(checkHexagonalDirection(source, target))
    }

    // --- Same feature: violations ---

    @Test
    fun `ports depending on app is a violation`() {
        val source = parseModulePath(":feat:projects:fe:ports")
        val target = parseModulePath(":feat:projects:fe:app:api")
        val violation = checkHexagonalDirection(source, target)
        assertEquals(RuleId.HEXAGONAL_DIRECTION, violation?.ruleId)
    }

    @Test
    fun `app depending on driving is a violation`() {
        val source = parseModulePath(":feat:projects:fe:app:impl")
        val target = parseModulePath(":feat:projects:fe:driving:api")
        val violation = checkHexagonalDirection(source, target)
        assertEquals(RuleId.HEXAGONAL_DIRECTION, violation?.ruleId)
    }

    @Test
    fun `driving depending on driven is a violation`() {
        val source = parseModulePath(":feat:projects:fe:driving:impl")
        val target = parseModulePath(":feat:projects:fe:driven:impl")
        val violation = checkHexagonalDirection(source, target)
        assertEquals(RuleId.HEXAGONAL_DIRECTION, violation?.ruleId)
    }

    @Test
    fun `driven depending on driving is a violation`() {
        val source = parseModulePath(":feat:projects:fe:driven:impl")
        val target = parseModulePath(":feat:projects:fe:driving:api")
        val violation = checkHexagonalDirection(source, target)
        assertEquals(RuleId.HEXAGONAL_DIRECTION, violation?.ruleId)
    }

    @Test
    fun `driving depending on ports is a violation`() {
        val source = parseModulePath(":feat:projects:fe:driving:impl")
        val target = parseModulePath(":feat:projects:fe:ports")
        val violation = checkHexagonalDirection(source, target)
        assertEquals(RuleId.HEXAGONAL_DIRECTION, violation?.ruleId)
    }

    @Test
    fun `cross-platform hex violation is caught`() {
        val source = parseModulePath(":feat:projects:fe:app:impl")
        val target = parseModulePath(":feat:projects:be:driving:impl")
        val violation = checkHexagonalDirection(source, target)
        assertEquals(RuleId.HEXAGONAL_DIRECTION, violation?.ruleId)
    }

    // --- Cross-feature ---

    @Test
    fun `cross-feature driving depending on app api is allowed`() {
        val source = parseModulePath(":feat:structures:fe:driving:impl")
        val target = parseModulePath(":feat:projects:fe:app:api")
        assertNull(checkHexagonalDirection(source, target))
    }

    @Test
    fun `cross-feature app depending on app api is allowed`() {
        val source = parseModulePath(":feat:structures:fe:app:impl")
        val target = parseModulePath(":feat:projects:fe:app:api")
        assertNull(checkHexagonalDirection(source, target))
    }

    @Test
    fun `cross-feature business depending on ports is allowed`() {
        val source = parseModulePath(":feat:structures:business:rules")
        val target = parseModulePath(":feat:projects:fe:ports")
        assertNull(checkHexagonalDirection(source, target))
    }

    @Test
    fun `cross-feature app depending on ports is a violation`() {
        val source = parseModulePath(":feat:structures:fe:app:impl")
        val target = parseModulePath(":feat:projects:fe:ports")
        val violation = checkHexagonalDirection(source, target)
        assertEquals(RuleId.HEXAGONAL_DIRECTION, violation?.ruleId)
    }

    @Test
    fun `cross-feature driven depending on ports is a violation`() {
        val source = parseModulePath(":feat:structures:fe:driven:impl")
        val target = parseModulePath(":feat:projects:fe:ports")
        val violation = checkHexagonalDirection(source, target)
        assertEquals(RuleId.HEXAGONAL_DIRECTION, violation?.ruleId)
    }

    @Test
    fun `cross-feature driving depending on ports is a violation`() {
        val source = parseModulePath(":feat:structures:fe:driving:impl")
        val target = parseModulePath(":feat:projects:fe:ports")
        val violation = checkHexagonalDirection(source, target)
        assertEquals(RuleId.HEXAGONAL_DIRECTION, violation?.ruleId)
    }

    // --- Skip conditions ---

    @Test
    fun `non-feat modules - rule does not apply`() {
        val source = parseModulePath(":lib:network:fe:impl")
        val target = parseModulePath(":lib:network:fe:api")
        assertNull(checkHexagonalDirection(source, target))
    }

    @Test
    fun `modules without hex layer - rule does not apply`() {
        val source = parseModulePath(":feat:sync:be:impl")
        val target = parseModulePath(":feat:sync:be:api")
        assertNull(checkHexagonalDirection(source, target))
    }
}
