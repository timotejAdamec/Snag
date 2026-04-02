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

class CategoryDirectionRuleTest {

    @Test
    fun `core depending on feat is a violation`() {
        val source = parseModulePath(":core:foundation:common")
        val target = parseModulePath(":feat:projects:business:model")
        val violation = checkCategoryDirection(source, target)
        assertEquals(RuleId.CATEGORY_DIRECTION, violation?.ruleId)
    }

    @Test
    fun `core depending on lib is a violation`() {
        val source = parseModulePath(":core:foundation:fe")
        val target = parseModulePath(":lib:design:fe")
        val violation = checkCategoryDirection(source, target)
        assertEquals(RuleId.CATEGORY_DIRECTION, violation?.ruleId)
    }

    @Test
    fun `lib depending on feat is a violation`() {
        val source = parseModulePath(":lib:network:fe:impl")
        val target = parseModulePath(":feat:projects:fe:app:api")
        val violation = checkCategoryDirection(source, target)
        assertEquals(RuleId.CATEGORY_DIRECTION, violation?.ruleId)
    }

    @Test
    fun `feat depending on app is a violation`() {
        val source = parseModulePath(":feat:projects:fe:driving:impl")
        val target = parseModulePath(":androidApp")
        val violation = checkCategoryDirection(source, target)
        assertEquals(RuleId.CATEGORY_DIRECTION, violation?.ruleId)
    }

    @Test
    fun `feat depending on lib is allowed`() {
        val source = parseModulePath(":feat:projects:fe:driving:impl")
        val target = parseModulePath(":lib:design:fe")
        assertNull(checkCategoryDirection(source, target))
    }

    @Test
    fun `feat depending on core is allowed`() {
        val source = parseModulePath(":feat:projects:fe:app:api")
        val target = parseModulePath(":core:foundation:common")
        assertNull(checkCategoryDirection(source, target))
    }

    @Test
    fun `lib depending on core is allowed`() {
        val source = parseModulePath(":lib:network:fe:impl")
        val target = parseModulePath(":core:network:fe")
        assertNull(checkCategoryDirection(source, target))
    }

    @Test
    fun `app depending on feat is allowed`() {
        val source = parseModulePath(":androidApp")
        val target = parseModulePath(":feat:projects:fe:driving:impl")
        assertNull(checkCategoryDirection(source, target))
    }

    @Test
    fun `infra module is exempt from category check`() {
        val source = parseModulePath(":testInfra:be")
        val target = parseModulePath(":feat:projects:be:app:impl")
        assertNull(checkCategoryDirection(source, target))
    }

    @Test
    fun `infra module as target is exempt`() {
        val source = parseModulePath(":core:foundation:be")
        val target = parseModulePath(":testInfra:common")
        assertNull(checkCategoryDirection(source, target))
    }

    @Test
    fun `feat depending on feat is allowed`() {
        val source = parseModulePath(":feat:structures:fe:driving:impl")
        val target = parseModulePath(":feat:projects:fe:app:api")
        assertNull(checkCategoryDirection(source, target))
    }

    @Test
    fun `core depending on core is allowed`() {
        val source = parseModulePath(":core:business:rules:impl")
        val target = parseModulePath(":core:foundation:common")
        assertNull(checkCategoryDirection(source, target))
    }
}
