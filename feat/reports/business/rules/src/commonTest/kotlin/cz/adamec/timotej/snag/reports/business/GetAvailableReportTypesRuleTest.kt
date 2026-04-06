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
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

class GetAvailableReportTypesRuleTest {
    private val rule = GetAvailableReportTypesRule()

    private fun createUser(role: UserRole?) =
        object : User {
            override val id: Uuid = UuidProvider.getUuid()
            override val email: String = "test@example.com"
            override val role: UserRole? = role
        }

    @Test
    fun `administrator gets all report types`() {
        assertEquals(
            ReportType.entries.toList(),
            rule(createUser(role = UserRole.ADMINISTRATOR)),
        )
    }

    @Test
    fun `passport lead gets only passport`() {
        assertEquals(
            listOf(ReportType.PASSPORT),
            rule(createUser(role = UserRole.PASSPORT_LEAD)),
        )
    }

    @Test
    fun `passport technician gets only passport`() {
        assertEquals(
            listOf(ReportType.PASSPORT),
            rule(createUser(role = UserRole.PASSPORT_TECHNICIAN)),
        )
    }

    @Test
    fun `service lead gets only service protocol`() {
        assertEquals(
            listOf(ReportType.SERVICE_PROTOCOL),
            rule(createUser(role = UserRole.SERVICE_LEAD)),
        )
    }

    @Test
    fun `service worker gets only service protocol`() {
        assertEquals(
            listOf(ReportType.SERVICE_PROTOCOL),
            rule(createUser(role = UserRole.SERVICE_WORKER)),
        )
    }

    @Test
    fun `user without role gets no report types`() {
        assertEquals(
            emptyList(),
            rule(createUser(role = null)),
        )
    }
}
