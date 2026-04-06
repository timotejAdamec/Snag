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

package cz.adamec.timotej.snag.reports.business

import cz.adamec.timotej.snag.authorization.business.UserRole
import cz.adamec.timotej.snag.core.foundation.common.UuidProvider
import cz.adamec.timotej.snag.users.business.User
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class CanGenerateReportRuleTest {
    private val rule = CanGenerateReportRule()

    private fun createUser(role: UserRole?) =
        object : User {
            override val id: Uuid = UuidProvider.getUuid()
            override val email: String = "test@example.com"
            override val role: UserRole? = role
        }

    @Test
    fun `administrator can generate passport report`() {
        assertTrue(rule(createUser(role = UserRole.ADMINISTRATOR), ReportType.PASSPORT))
    }

    @Test
    fun `administrator can generate service protocol`() {
        assertTrue(rule(createUser(role = UserRole.ADMINISTRATOR), ReportType.SERVICE_PROTOCOL))
    }

    @Test
    fun `passport lead can generate passport report`() {
        assertTrue(rule(createUser(role = UserRole.PASSPORT_LEAD), ReportType.PASSPORT))
    }

    @Test
    fun `passport lead cannot generate service protocol`() {
        assertFalse(rule(createUser(role = UserRole.PASSPORT_LEAD), ReportType.SERVICE_PROTOCOL))
    }

    @Test
    fun `passport technician can generate passport report`() {
        assertTrue(rule(createUser(role = UserRole.PASSPORT_TECHNICIAN), ReportType.PASSPORT))
    }

    @Test
    fun `passport technician cannot generate service protocol`() {
        assertFalse(rule(createUser(role = UserRole.PASSPORT_TECHNICIAN), ReportType.SERVICE_PROTOCOL))
    }

    @Test
    fun `service lead can generate service protocol`() {
        assertTrue(rule(createUser(role = UserRole.SERVICE_LEAD), ReportType.SERVICE_PROTOCOL))
    }

    @Test
    fun `service lead cannot generate passport report`() {
        assertFalse(rule(createUser(role = UserRole.SERVICE_LEAD), ReportType.PASSPORT))
    }

    @Test
    fun `service worker can generate service protocol`() {
        assertTrue(rule(createUser(role = UserRole.SERVICE_WORKER), ReportType.SERVICE_PROTOCOL))
    }

    @Test
    fun `service worker cannot generate passport report`() {
        assertFalse(rule(createUser(role = UserRole.SERVICE_WORKER), ReportType.PASSPORT))
    }

    @Test
    fun `user without role cannot generate any report`() {
        assertFalse(rule(createUser(role = null), ReportType.PASSPORT))
        assertFalse(rule(createUser(role = null), ReportType.SERVICE_PROTOCOL))
    }
}
