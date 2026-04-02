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
