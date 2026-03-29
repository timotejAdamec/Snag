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

package cz.adamec.timotej.snag.projects.business

import cz.adamec.timotej.snag.authorization.business.UserRole
import cz.adamec.timotej.snag.core.foundation.common.UuidProvider
import cz.adamec.timotej.snag.users.business.User
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class CanCreateProjectRuleTest {
    private val rule = CanCreateProjectRule()

    private fun createUser(role: UserRole?) =
        object : User {
            override val id: Uuid = UuidProvider.getUuid()
            override val email: String = "test@example.com"
            override val role: UserRole? = role
        }

    @Test
    fun `returns true for ADMINISTRATOR`() {
        assertTrue(rule(createUser(role = UserRole.ADMINISTRATOR)))
    }

    @Test
    fun `returns true for PASSPORT_LEAD`() {
        assertTrue(rule(createUser(role = UserRole.PASSPORT_LEAD)))
    }

    @Test
    fun `returns false for PASSPORT_TECHNICIAN`() {
        assertFalse(rule(createUser(role = UserRole.PASSPORT_TECHNICIAN)))
    }

    @Test
    fun `returns true for SERVICE_LEAD`() {
        assertTrue(rule(createUser(role = UserRole.SERVICE_LEAD)))
    }

    @Test
    fun `returns true for SERVICE_WORKER`() {
        assertTrue(rule(createUser(role = UserRole.SERVICE_WORKER)))
    }

    @Test
    fun `returns false for null role`() {
        assertFalse(rule(createUser(role = null)))
    }
}
