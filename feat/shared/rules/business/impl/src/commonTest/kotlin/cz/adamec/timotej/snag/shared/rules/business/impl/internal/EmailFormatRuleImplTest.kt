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

class EmailFormatRuleImplTest {
    private val rule = EmailFormatRuleImpl()

    @Test
    fun `simple email is valid`() {
        assertTrue(rule("user@example.com"))
    }

    @Test
    fun `email with dot in local part is valid`() {
        assertTrue(rule("user.name@domain.org"))
    }

    @Test
    fun `email with plus tag is valid`() {
        assertTrue(rule("user+tag@domain.com"))
    }

    @Test
    fun `email with underscore in local part is valid`() {
        assertTrue(rule("user_name@domain.co.uk"))
    }

    @Test
    fun `email with subdomain is valid`() {
        assertTrue(rule("test@sub.domain.com"))
    }

    @Test
    fun `uppercase email is valid`() {
        assertTrue(rule("USER@DOMAIN.COM"))
    }

    @Test
    fun `minimal email is valid`() {
        assertTrue(rule("a@b.cc"))
    }

    @Test
    fun `empty string is invalid`() {
        assertFalse(rule(""))
    }

    @Test
    fun `plain text without at sign is invalid`() {
        assertFalse(rule("plaintext"))
    }

    @Test
    fun `missing local part is invalid`() {
        assertFalse(rule("@domain.com"))
    }

    @Test
    fun `missing domain is invalid`() {
        assertFalse(rule("user@"))
    }

    @Test
    fun `missing TLD is invalid`() {
        assertFalse(rule("user@domain"))
    }

    @Test
    fun `single char TLD is invalid`() {
        assertFalse(rule("user@domain.c"))
    }

    @Test
    fun `space in local part is invalid`() {
        assertFalse(rule("user @domain.com"))
    }

    @Test
    fun `space in domain is invalid`() {
        assertFalse(rule("user@dom ain.com"))
    }

    @Test
    fun `domain starting with dot is invalid`() {
        assertFalse(rule("user@.domain.com"))
    }

    @Test
    fun `consecutive dots in domain is invalid`() {
        assertFalse(rule("user@domain..com"))
    }
}
