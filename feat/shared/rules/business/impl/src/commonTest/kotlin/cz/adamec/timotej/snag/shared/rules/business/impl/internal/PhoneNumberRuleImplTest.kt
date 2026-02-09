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

package cz.adamec.timotej.snag.shared.rules.business.impl.internal

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PhoneNumberRuleImplTest {
    private val rule = PhoneNumberRuleImpl()

    @Test
    fun `international format is valid`() {
        assertTrue(rule("+1234567890"))
    }

    @Test
    fun `dashes format is valid`() {
        assertTrue(rule("123-456-7890"))
    }

    @Test
    fun `parentheses format is valid`() {
        assertTrue(rule("(123) 456-7890"))
    }

    @Test
    fun `digits only is valid`() {
        assertTrue(rule("123"))
    }

    @Test
    fun `long number with country code is valid`() {
        assertTrue(rule("00420123456789"))
    }

    @Test
    fun `empty string is invalid`() {
        assertFalse(rule(""))
    }

    @Test
    fun `blank string is invalid`() {
        assertFalse(rule("   "))
    }

    @Test
    fun `letters only is invalid`() {
        assertFalse(rule("abc"))
    }

    @Test
    fun `mixed letters and digits is invalid`() {
        assertFalse(rule("123abc456"))
    }

    @Test
    fun `plus sign only is invalid`() {
        assertFalse(rule("+"))
    }

    @Test
    fun `dashes only is invalid`() {
        assertFalse(rule("---"))
    }

    @Test
    fun `surrounding spaces is invalid`() {
        assertFalse(rule(" 123 "))
    }
}
