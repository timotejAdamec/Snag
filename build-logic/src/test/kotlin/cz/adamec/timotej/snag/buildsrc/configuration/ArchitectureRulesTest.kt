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

package cz.adamec.timotej.snag.buildsrc.configuration

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ParseModulePathTest {

    // --- Core modules ---

    @Test
    fun `core foundation common`() {
        val result = parseModulePath(":core:foundation:common")
        assertIs<CoreModule>(result)
        assertEquals("foundation", result.name)
        assertEquals(Platform.COMMON, result.platform)
        assertNull(result.encapsulation)
    }

    @Test
    fun `core foundation fe`() {
        val result = parseModulePath(":core:foundation:fe")
        assertIs<CoreModule>(result)
        assertEquals("foundation", result.name)
        assertEquals(Platform.FE, result.platform)
    }

    @Test
    fun `core foundation be`() {
        val result = parseModulePath(":core:foundation:be")
        assertIs<CoreModule>(result)
        assertEquals("foundation", result.name)
        assertEquals(Platform.BE, result.platform)
    }

    @Test
    fun `core business rules api`() {
        val result = parseModulePath(":core:business:rules:api")
        assertIs<CoreModule>(result)
        assertEquals("business:rules", result.name)
        assertEquals(Encapsulation.API, result.encapsulation)
    }

    @Test
    fun `core business rules impl`() {
        val result = parseModulePath(":core:business:rules:impl")
        assertIs<CoreModule>(result)
        assertEquals("business:rules", result.name)
        assertEquals(Encapsulation.IMPL, result.encapsulation)
    }

    @Test
    fun `core storage fe`() {
        val result = parseModulePath(":core:storage:fe")
        assertIs<CoreModule>(result)
        assertEquals("storage", result.name)
        assertEquals(Platform.FE, result.platform)
    }

    @Test
    fun `core network fe`() {
        val result = parseModulePath(":core:network:fe")
        assertIs<CoreModule>(result)
        assertEquals("network", result.name)
        assertEquals(Platform.FE, result.platform)
    }

    // --- Lib modules ---

    @Test
    fun `lib design fe`() {
        val result = parseModulePath(":lib:design:fe")
        assertIs<LibModule>(result)
        assertEquals("design", result.name)
        assertEquals(Platform.FE, result.platform)
    }

    @Test
    fun `lib routing common`() {
        val result = parseModulePath(":lib:routing:common")
        assertIs<LibModule>(result)
        assertEquals("routing", result.name)
        assertEquals(Platform.COMMON, result.platform)
    }

    @Test
    fun `lib storage contract`() {
        val result = parseModulePath(":lib:storage:contract")
        assertIs<LibModule>(result)
        assertEquals("storage", result.name)
        assertEquals(Encapsulation.CONTRACT, result.encapsulation)
    }

    @Test
    fun `lib network fe api`() {
        val result = parseModulePath(":lib:network:fe:api")
        assertIs<LibModule>(result)
        assertEquals("network", result.name)
        assertEquals(Platform.FE, result.platform)
        assertEquals(Encapsulation.API, result.encapsulation)
    }

    @Test
    fun `lib network fe impl`() {
        val result = parseModulePath(":lib:network:fe:impl")
        assertIs<LibModule>(result)
        assertEquals("network", result.name)
        assertEquals(Platform.FE, result.platform)
        assertEquals(Encapsulation.IMPL, result.encapsulation)
    }

    @Test
    fun `lib configuration common api`() {
        val result = parseModulePath(":lib:configuration:common:api")
        assertIs<LibModule>(result)
        assertEquals("configuration", result.name)
        assertEquals(Platform.COMMON, result.platform)
        assertEquals(Encapsulation.API, result.encapsulation)
    }

    @Test
    fun `lib database fe test`() {
        val result = parseModulePath(":lib:database:fe:test")
        assertIs<LibModule>(result)
        assertEquals("database", result.name)
        assertEquals(Platform.FE, result.platform)
        assertEquals(Encapsulation.TEST, result.encapsulation)
    }

    // --- Feat modules: standard ---

    @Test
    fun `feat projects business model`() {
        val result = parseModulePath(":feat:projects:business:model")
        assertIs<FeatModule>(result)
        assertEquals("projects", result.feature)
        assertNull(result.platform)
        assertEquals(HexLayer.BUSINESS, result.hexLayer)
        assertTrue(result.isModel)
    }

    @Test
    fun `feat projects business rules`() {
        val result = parseModulePath(":feat:projects:business:rules")
        assertIs<FeatModule>(result)
        assertEquals("projects", result.feature)
        assertEquals(HexLayer.BUSINESS, result.hexLayer)
        assertTrue(result.isRules)
    }

    @Test
    fun `feat projects app model`() {
        val result = parseModulePath(":feat:projects:app:model")
        assertIs<FeatModule>(result)
        assertEquals("projects", result.feature)
        assertEquals(HexLayer.APP, result.hexLayer)
        assertTrue(result.isModel)
    }

    @Test
    fun `feat projects fe app api`() {
        val result = parseModulePath(":feat:projects:fe:app:api")
        assertIs<FeatModule>(result)
        assertEquals("projects", result.feature)
        assertEquals(Platform.FE, result.platform)
        assertEquals(HexLayer.APP, result.hexLayer)
        assertEquals(Encapsulation.API, result.encapsulation)
    }

    @Test
    fun `feat projects fe driving impl`() {
        val result = parseModulePath(":feat:projects:fe:driving:impl")
        assertIs<FeatModule>(result)
        assertEquals("projects", result.feature)
        assertEquals(Platform.FE, result.platform)
        assertEquals(HexLayer.DRIVING, result.hexLayer)
        assertEquals(Encapsulation.IMPL, result.encapsulation)
    }

    @Test
    fun `feat projects be driven test`() {
        val result = parseModulePath(":feat:projects:be:driven:test")
        assertIs<FeatModule>(result)
        assertEquals("projects", result.feature)
        assertEquals(Platform.BE, result.platform)
        assertEquals(HexLayer.DRIVEN, result.hexLayer)
        assertEquals(Encapsulation.TEST, result.encapsulation)
    }

    @Test
    fun `feat projects fe ports`() {
        val result = parseModulePath(":feat:projects:fe:ports")
        assertIs<FeatModule>(result)
        assertEquals("projects", result.feature)
        assertEquals(Platform.FE, result.platform)
        assertEquals(HexLayer.PORTS, result.hexLayer)
    }

    @Test
    fun `feat projects be driving contract`() {
        val result = parseModulePath(":feat:projects:be:driving:contract")
        assertIs<FeatModule>(result)
        assertEquals("projects", result.feature)
        assertEquals(Platform.BE, result.platform)
        assertEquals(HexLayer.DRIVING, result.hexLayer)
        assertEquals(Encapsulation.CONTRACT, result.encapsulation)
    }

    @Test
    fun `feat projects be app model`() {
        val result = parseModulePath(":feat:projects:be:app:model")
        assertIs<FeatModule>(result)
        assertEquals("projects", result.feature)
        assertEquals(Platform.BE, result.platform)
        assertEquals(HexLayer.APP, result.hexLayer)
        assertTrue(result.isModel)
    }

    // --- Feat modules: shared (two-segment feature name) ---

    @Test
    fun `feat shared database fe impl`() {
        val result = parseModulePath(":feat:shared:database:fe:impl")
        assertIs<FeatModule>(result)
        assertEquals("shared:database", result.feature)
        assertEquals(Platform.FE, result.platform)
        assertEquals(Encapsulation.IMPL, result.encapsulation)
    }

    @Test
    fun `feat shared database fe api`() {
        val result = parseModulePath(":feat:shared:database:fe:api")
        assertIs<FeatModule>(result)
        assertEquals("shared:database", result.feature)
        assertEquals(Platform.FE, result.platform)
        assertEquals(Encapsulation.API, result.encapsulation)
    }

    @Test
    fun `feat shared storage be`() {
        val result = parseModulePath(":feat:shared:storage:be")
        assertIs<FeatModule>(result)
        assertEquals("shared:storage", result.feature)
        assertEquals(Platform.BE, result.platform)
    }

    @Test
    fun `feat shared storage fe`() {
        val result = parseModulePath(":feat:shared:storage:fe")
        assertIs<FeatModule>(result)
        assertEquals("shared:storage", result.feature)
        assertEquals(Platform.FE, result.platform)
    }

    // --- Feat modules: sync (irregular) ---

    @Test
    fun `feat sync model - no platform`() {
        val result = parseModulePath(":feat:sync:model")
        assertIs<FeatModule>(result)
        assertEquals("sync", result.feature)
        assertNull(result.platform)
        assertTrue(result.isModel)
    }

    @Test
    fun `feat sync be api - no hex layer`() {
        val result = parseModulePath(":feat:sync:be:api")
        assertIs<FeatModule>(result)
        assertEquals("sync", result.feature)
        assertEquals(Platform.BE, result.platform)
        assertEquals(Encapsulation.API, result.encapsulation)
        assertNull(result.hexLayer)
    }

    @Test
    fun `feat sync be impl`() {
        val result = parseModulePath(":feat:sync:be:impl")
        assertIs<FeatModule>(result)
        assertEquals("sync", result.feature)
        assertEquals(Platform.BE, result.platform)
        assertEquals(Encapsulation.IMPL, result.encapsulation)
    }

    @Test
    fun `feat sync be model`() {
        val result = parseModulePath(":feat:sync:be:model")
        assertIs<FeatModule>(result)
        assertEquals("sync", result.feature)
        assertEquals(Platform.BE, result.platform)
        assertTrue(result.isModel)
    }

    @Test
    fun `feat sync fe model`() {
        val result = parseModulePath(":feat:sync:fe:model")
        assertIs<FeatModule>(result)
        assertEquals("sync", result.feature)
        assertEquals(Platform.FE, result.platform)
        assertTrue(result.isModel)
    }

    @Test
    fun `feat sync fe app api`() {
        val result = parseModulePath(":feat:sync:fe:app:api")
        assertIs<FeatModule>(result)
        assertEquals("sync", result.feature)
        assertEquals(Platform.FE, result.platform)
        assertEquals(HexLayer.APP, result.hexLayer)
        assertEquals(Encapsulation.API, result.encapsulation)
    }

    // --- App modules ---

    @Test
    fun `androidApp`() {
        val result = parseModulePath(":androidApp")
        assertIs<AppModule>(result)
        assertEquals("androidApp", result.name)
    }

    @Test
    fun `composeApp`() {
        val result = parseModulePath(":composeApp")
        assertIs<AppModule>(result)
        assertEquals("composeApp", result.name)
    }

    // --- Infra modules ---

    @Test
    fun `testInfra be`() {
        val result = parseModulePath(":testInfra:be")
        assertIs<InfraModule>(result)
        assertEquals("testInfra:be", result.name)
    }

    @Test
    fun `testInfra common`() {
        val result = parseModulePath(":testInfra:common")
        assertIs<InfraModule>(result)
        assertEquals("testInfra:common", result.name)
    }

    @Test
    fun `koinModulesAggregate fe`() {
        val result = parseModulePath(":koinModulesAggregate:fe")
        assertIs<InfraModule>(result)
        assertEquals("koinModulesAggregate:fe", result.name)
    }

    @Test
    fun `server api`() {
        val result = parseModulePath(":server:api")
        assertIs<InfraModule>(result)
        assertEquals("server:api", result.name)
    }

    @Test
    fun `server impl`() {
        val result = parseModulePath(":server:impl")
        assertIs<InfraModule>(result)
        assertEquals("server:impl", result.name)
    }
}

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

class HexagonalDirectionRuleTest {

    @Test
    fun `driving depending on app is allowed`() {
        val source = parseModulePath(":feat:projects:fe:driving:impl")
        val target = parseModulePath(":feat:projects:fe:app:api")
        assertNull(checkHexagonalDirection(source, target))
    }

    @Test
    fun `app depending on business is allowed`() {
        val source = parseModulePath(":feat:projects:fe:app:impl")
        val target = parseModulePath(":feat:projects:business:model")
        // Different platform (fe vs null) — rule does not apply
        assertNull(checkHexagonalDirection(source, target))
    }

    @Test
    fun `app depending on ports is allowed within same feature and platform`() {
        val source = parseModulePath(":feat:projects:fe:app:impl")
        val target = parseModulePath(":feat:projects:fe:ports")
        assertNull(checkHexagonalDirection(source, target))
    }

    @Test
    fun `business depending on app is a violation`() {
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
    fun `different features - rule does not apply`() {
        val source = parseModulePath(":feat:projects:fe:app:impl")
        val target = parseModulePath(":feat:findings:fe:driving:api")
        assertNull(checkHexagonalDirection(source, target))
    }

    @Test
    fun `different platforms - rule does not apply`() {
        val source = parseModulePath(":feat:projects:fe:app:impl")
        val target = parseModulePath(":feat:projects:be:driving:impl")
        assertNull(checkHexagonalDirection(source, target))
    }

    @Test
    fun `non-feat modules - rule does not apply`() {
        val source = parseModulePath(":lib:network:fe:impl")
        val target = parseModulePath(":lib:network:fe:api")
        assertNull(checkHexagonalDirection(source, target))
    }

    @Test
    fun `driven depending on ports is allowed`() {
        val source = parseModulePath(":feat:projects:fe:driven:impl")
        val target = parseModulePath(":feat:projects:fe:ports")
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
        // Different platforms (null vs null) and isModel target — exempt
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
    fun `test depending on impl is allowed`() {
        val source = parseModulePath(":feat:projects:fe:driven:test")
        val target = parseModulePath(":feat:projects:fe:driven:impl")
        assertNull(checkEncapsulationDirection(source, target))
    }

    @Test
    fun `modules without encapsulation - rule does not apply`() {
        val source = parseModulePath(":feat:projects:fe:ports")
        val target = parseModulePath(":feat:projects:business:model")
        assertNull(checkEncapsulationDirection(source, target))
    }

    @Test
    fun `contract depending on api is allowed`() {
        val source = parseModulePath(":lib:storage:contract")
        val target = parseModulePath(":lib:configuration:common:api")
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
