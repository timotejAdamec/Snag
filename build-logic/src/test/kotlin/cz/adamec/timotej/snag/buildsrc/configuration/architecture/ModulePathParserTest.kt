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
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ParseModulePathTest {

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
    fun `feat projects contract`() {
        val result = parseModulePath(":feat:projects:contract")
        assertIs<FeatModule>(result)
        assertEquals("projects", result.feature)
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

    @Test
    fun `featShared database fe driven impl`() {
        val result = parseModulePath(":featShared:database:fe:driven:impl")
        assertIs<FeatSharedModule>(result)
        assertEquals("database", result.feature)
        assertEquals(Platform.FE, result.platform)
        assertEquals(HexLayer.DRIVEN, result.hexLayer)
        assertEquals(Encapsulation.IMPL, result.encapsulation)
    }

    @Test
    fun `featShared database fe driven api`() {
        val result = parseModulePath(":featShared:database:fe:driven:api")
        assertIs<FeatSharedModule>(result)
        assertEquals("database", result.feature)
        assertEquals(Platform.FE, result.platform)
        assertEquals(HexLayer.DRIVEN, result.hexLayer)
        assertEquals(Encapsulation.API, result.encapsulation)
    }

    @Test
    fun `featShared database be driven api`() {
        val result = parseModulePath(":featShared:database:be:driven:api")
        assertIs<FeatSharedModule>(result)
        assertEquals("database", result.feature)
        assertEquals(Platform.BE, result.platform)
        assertEquals(HexLayer.DRIVEN, result.hexLayer)
        assertEquals(Encapsulation.API, result.encapsulation)
    }

    @Test
    fun `featShared database be driven impl`() {
        val result = parseModulePath(":featShared:database:be:driven:impl")
        assertIs<FeatSharedModule>(result)
        assertEquals("database", result.feature)
        assertEquals(Platform.BE, result.platform)
        assertEquals(HexLayer.DRIVEN, result.hexLayer)
        assertEquals(Encapsulation.IMPL, result.encapsulation)
    }

    @Test
    fun `featShared storage be driven impl`() {
        val result = parseModulePath(":featShared:storage:be:driven:impl")
        assertIs<FeatSharedModule>(result)
        assertEquals("storage", result.feature)
        assertEquals(Platform.BE, result.platform)
        assertEquals(HexLayer.DRIVEN, result.hexLayer)
        assertEquals(Encapsulation.IMPL, result.encapsulation)
    }

    @Test
    fun `featShared storage fe driven impl`() {
        val result = parseModulePath(":featShared:storage:fe:driven:impl")
        assertIs<FeatSharedModule>(result)
        assertEquals("storage", result.feature)
        assertEquals(Platform.FE, result.platform)
        assertEquals(HexLayer.DRIVEN, result.hexLayer)
        assertEquals(Encapsulation.IMPL, result.encapsulation)
    }

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
    fun `server`() {
        val result = parseModulePath(":server")
        assertIs<AppModule>(result)
        assertEquals("server", result.name)
    }
}
