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

package cz.adamec.timotej.snag.clients.business

import cz.adamec.timotej.snag.authorization.business.UserRole
import cz.adamec.timotej.snag.core.foundation.common.UuidProvider
import cz.adamec.timotej.snag.users.business.User
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class CanManageClientsRuleTest {
    private val rule = CanManageClientsRule()

    private fun createUser(role: UserRole?) =
        object : User {
            override val id: Uuid = UuidProvider.getUuid()
            override val entraId: String = "entra"
            override val email: String = "test@example.com"
            override val role: UserRole? = role
        }

    @Test
    fun `administrator can manage clients`() {
        assertTrue(rule(createUser(role = UserRole.ADMINISTRATOR)))
    }

    @Test
    fun `passport lead can manage clients`() {
        assertTrue(rule(createUser(role = UserRole.PASSPORT_LEAD)))
    }

    @Test
    fun `service lead can manage clients`() {
        assertTrue(rule(createUser(role = UserRole.SERVICE_LEAD)))
    }

    @Test
    fun `service worker can manage clients`() {
        assertTrue(rule(createUser(role = UserRole.SERVICE_WORKER)))
    }

    @Test
    fun `passport technician cannot manage clients`() {
        assertFalse(rule(createUser(role = UserRole.PASSPORT_TECHNICIAN)))
    }

    @Test
    fun `user without role cannot manage clients`() {
        assertFalse(rule(createUser(role = null)))
    }
}
