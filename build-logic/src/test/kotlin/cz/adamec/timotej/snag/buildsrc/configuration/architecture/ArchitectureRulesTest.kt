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
import kotlin.test.assertTrue

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

class PlatformDirectionRuleTest {

    @Test
    fun `platform-agnostic depending on FE is a violation`() {
        val source = parseModulePath(":feat:projects:app:model")
        val target = parseModulePath(":feat:projects:fe:app:model")
        val violation = checkPlatformDirection(source, target)
        assertEquals(RuleId.PLATFORM_DIRECTION, violation?.ruleId)
    }

    @Test
    fun `platform-agnostic depending on BE is a violation`() {
        val source = parseModulePath(":feat:projects:app:model")
        val target = parseModulePath(":feat:projects:be:app:model")
        val violation = checkPlatformDirection(source, target)
        assertEquals(RuleId.PLATFORM_DIRECTION, violation?.ruleId)
    }

    @Test
    fun `COMMON depending on FE is a violation within feat`() {
        val source = parseModulePath(":feat:sync:model")
        val target = parseModulePath(":feat:sync:fe:model")
        val violation = checkPlatformDirection(source, target)
        assertEquals(RuleId.PLATFORM_DIRECTION, violation?.ruleId)
    }

    @Test
    fun `COMMON depending on BE is a violation within feat`() {
        val source = parseModulePath(":feat:sync:model")
        val target = parseModulePath(":feat:sync:be:model")
        val violation = checkPlatformDirection(source, target)
        assertEquals(RuleId.PLATFORM_DIRECTION, violation?.ruleId)
    }

    @Test
    fun `FE depending on BE is a violation`() {
        val source = parseModulePath(":feat:projects:fe:app:impl")
        val target = parseModulePath(":feat:projects:be:app:api")
        val violation = checkPlatformDirection(source, target)
        assertEquals(RuleId.PLATFORM_DIRECTION, violation?.ruleId)
    }

    @Test
    fun `BE depending on FE is a violation`() {
        val source = parseModulePath(":feat:projects:be:app:impl")
        val target = parseModulePath(":feat:projects:fe:app:api")
        val violation = checkPlatformDirection(source, target)
        assertEquals(RuleId.PLATFORM_DIRECTION, violation?.ruleId)
    }

    @Test
    fun `FE depending on platform-agnostic is allowed`() {
        val source = parseModulePath(":feat:projects:fe:app:impl")
        val target = parseModulePath(":feat:projects:app:model")
        assertNull(checkPlatformDirection(source, target))
    }

    @Test
    fun `BE depending on platform-agnostic is allowed`() {
        val source = parseModulePath(":feat:projects:be:app:impl")
        val target = parseModulePath(":feat:projects:business:model")
        assertNull(checkPlatformDirection(source, target))
    }

    @Test
    fun `null depending on COMMON is allowed`() {
        val source = parseModulePath(":feat:projects:business:model")
        val target = parseModulePath(":feat:projects:business:model")
        assertNull(checkPlatformDirection(source, target))
    }

    @Test
    fun `same platform is allowed`() {
        val source = parseModulePath(":feat:projects:fe:driving:impl")
        val target = parseModulePath(":feat:projects:fe:app:api")
        assertNull(checkPlatformDirection(source, target))
    }

    @Test
    fun `different features - rule does not apply`() {
        val source = parseModulePath(":feat:projects:app:model")
        val target = parseModulePath(":feat:findings:fe:app:api")
        assertNull(checkPlatformDirection(source, target))
    }

    @Test
    fun `non-feat modules - rule does not apply`() {
        val source = parseModulePath(":lib:routing:common")
        val target = parseModulePath(":lib:routing:fe")
        assertNull(checkPlatformDirection(source, target))
    }
}

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

