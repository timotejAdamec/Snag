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

package cz.adamec.timotej.snag.users.business

import cz.adamec.timotej.snag.authorization.business.UserRole
import cz.adamec.timotej.snag.core.foundation.common.UuidProvider
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class CanSetUserRoleRuleTest {
    private val rule = CanSetUserRoleRule()

    private fun createUser(role: UserRole?) =
        object : User {
            override val id: Uuid = UuidProvider.getUuid()
            override val entraId: String = "entra"
            override val email: String = "test@example.com"
            override val role: UserRole? = role
        }

    @Test
    fun `administrator can set any role`() {
        val admin = createUser(role = UserRole.ADMINISTRATOR)
        assertTrue(rule(actingUser = admin, targetCurrentRole = null, newRole = UserRole.PASSPORT_LEAD))
        assertTrue(rule(actingUser = admin, targetCurrentRole = null, newRole = UserRole.SERVICE_WORKER))
        assertTrue(rule(actingUser = admin, targetCurrentRole = UserRole.PASSPORT_LEAD, newRole = null))
    }

    @Test
    fun `passport lead can assign passport technician role`() {
        val lead = createUser(role = UserRole.PASSPORT_LEAD)
        assertTrue(rule(actingUser = lead, targetCurrentRole = null, newRole = UserRole.PASSPORT_TECHNICIAN))
    }

    @Test
    fun `passport lead can remove passport technician role`() {
        val lead = createUser(role = UserRole.PASSPORT_LEAD)
        assertTrue(rule(actingUser = lead, targetCurrentRole = UserRole.PASSPORT_TECHNICIAN, newRole = null))
    }

    @Test
    fun `passport lead cannot assign service worker role`() {
        val lead = createUser(role = UserRole.PASSPORT_LEAD)
        assertFalse(rule(actingUser = lead, targetCurrentRole = null, newRole = UserRole.SERVICE_WORKER))
    }

    @Test
    fun `service lead can assign service worker role`() {
        val lead = createUser(role = UserRole.SERVICE_LEAD)
        assertTrue(rule(actingUser = lead, targetCurrentRole = null, newRole = UserRole.SERVICE_WORKER))
    }

    @Test
    fun `service lead can remove service worker role`() {
        val lead = createUser(role = UserRole.SERVICE_LEAD)
        assertTrue(rule(actingUser = lead, targetCurrentRole = UserRole.SERVICE_WORKER, newRole = null))
    }

    @Test
    fun `service lead cannot assign passport technician role`() {
        val lead = createUser(role = UserRole.SERVICE_LEAD)
        assertFalse(rule(actingUser = lead, targetCurrentRole = null, newRole = UserRole.PASSPORT_TECHNICIAN))
    }

    @Test
    fun `passport technician cannot set any role`() {
        val tech = createUser(role = UserRole.PASSPORT_TECHNICIAN)
        assertFalse(rule(actingUser = tech, targetCurrentRole = null, newRole = UserRole.PASSPORT_TECHNICIAN))
    }

    @Test
    fun `service worker cannot set any role`() {
        val worker = createUser(role = UserRole.SERVICE_WORKER)
        assertFalse(rule(actingUser = worker, targetCurrentRole = null, newRole = UserRole.SERVICE_WORKER))
    }

    @Test
    fun `user without role cannot set any role`() {
        val noRole = createUser(role = null)
        assertFalse(rule(actingUser = noRole, targetCurrentRole = null, newRole = UserRole.ADMINISTRATOR))
    }
}
