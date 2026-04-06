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

package cz.adamec.timotej.snag.feat.reports.fe.app.impl.internal

import cz.adamec.timotej.snag.authorization.business.UserRole
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.feat.reports.fe.app.api.GetAvailableReportTypesFlowUseCase
import cz.adamec.timotej.snag.reports.business.ReportType
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import cz.adamec.timotej.snag.users.app.model.AppUserData
import cz.adamec.timotej.snag.users.fe.driven.test.FakeUsersDb
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

class GetAvailableReportTypesFlowUseCaseImplTest : FrontendKoinInitializedTest() {
    private val useCase: GetAvailableReportTypesFlowUseCase by inject()
    private val fakeUsersDb: FakeUsersDb by inject()

    private fun seedUser(role: UserRole?) {
        fakeUsersDb.setUser(
            AppUserData(
                id = Uuid.random(),
                authProviderId = "mock-auth-provider-id",
                email = "test@example.com",
                role = role,
                updatedAt = Timestamp(0L),
            ),
        )
    }

    @Test
    fun `administrator gets all report types`() =
        runTest(testDispatcher) {
            seedUser(role = UserRole.ADMINISTRATOR)

            val result = useCase().first()

            assertEquals(ReportType.entries.toList(), result)
        }

    @Test
    fun `passport lead gets only passport`() =
        runTest(testDispatcher) {
            seedUser(role = UserRole.PASSPORT_LEAD)

            val result = useCase().first()

            assertEquals(listOf(ReportType.PASSPORT), result)
        }

    @Test
    fun `service worker gets only service protocol`() =
        runTest(testDispatcher) {
            seedUser(role = UserRole.SERVICE_WORKER)

            val result = useCase().first()

            assertEquals(listOf(ReportType.SERVICE_PROTOCOL), result)
        }

    @Test
    fun `user without role gets empty list`() =
        runTest(testDispatcher) {
            seedUser(role = null)

            val result = useCase().first()

            assertEquals(emptyList(), result)
        }
}
