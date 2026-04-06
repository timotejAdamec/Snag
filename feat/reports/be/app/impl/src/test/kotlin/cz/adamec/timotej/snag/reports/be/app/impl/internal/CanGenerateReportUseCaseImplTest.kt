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

package cz.adamec.timotej.snag.reports.be.app.impl.internal

import cz.adamec.timotej.snag.authorization.business.UserRole
import cz.adamec.timotej.snag.reports.be.app.api.CanGenerateReportUseCase
import cz.adamec.timotej.snag.reports.business.ReportType
import cz.adamec.timotej.snag.testinfra.be.BackendKoinInitializedTest
import cz.adamec.timotej.snag.users.be.driven.test.TEST_USER_ID
import cz.adamec.timotej.snag.users.be.driven.test.seedTestUser
import cz.adamec.timotej.snag.users.be.ports.UsersDb
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class CanGenerateReportUseCaseImplTest : BackendKoinInitializedTest() {
    private val useCase: CanGenerateReportUseCase by inject()
    private val usersDb: UsersDb by inject()

    @Test
    fun `returns false when user does not exist`() =
        runTest(testDispatcher) {
            assertFalse(useCase(userId = Uuid.random(), type = ReportType.PASSPORT))
        }

    @Test
    fun `administrator can generate passport report`() =
        runTest(testDispatcher) {
            usersDb.seedTestUser(role = UserRole.ADMINISTRATOR)

            assertTrue(useCase(userId = TEST_USER_ID, type = ReportType.PASSPORT))
        }

    @Test
    fun `administrator can generate service protocol`() =
        runTest(testDispatcher) {
            usersDb.seedTestUser(role = UserRole.ADMINISTRATOR)

            assertTrue(useCase(userId = TEST_USER_ID, type = ReportType.SERVICE_PROTOCOL))
        }

    @Test
    fun `passport lead can generate passport report`() =
        runTest(testDispatcher) {
            usersDb.seedTestUser(role = UserRole.PASSPORT_LEAD)

            assertTrue(useCase(userId = TEST_USER_ID, type = ReportType.PASSPORT))
        }

    @Test
    fun `passport lead cannot generate service protocol`() =
        runTest(testDispatcher) {
            usersDb.seedTestUser(role = UserRole.PASSPORT_LEAD)

            assertFalse(useCase(userId = TEST_USER_ID, type = ReportType.SERVICE_PROTOCOL))
        }

    @Test
    fun `service worker can generate service protocol`() =
        runTest(testDispatcher) {
            usersDb.seedTestUser(role = UserRole.SERVICE_WORKER)

            assertTrue(useCase(userId = TEST_USER_ID, type = ReportType.SERVICE_PROTOCOL))
        }

    @Test
    fun `service worker cannot generate passport report`() =
        runTest(testDispatcher) {
            usersDb.seedTestUser(role = UserRole.SERVICE_WORKER)

            assertFalse(useCase(userId = TEST_USER_ID, type = ReportType.PASSPORT))
        }
}
