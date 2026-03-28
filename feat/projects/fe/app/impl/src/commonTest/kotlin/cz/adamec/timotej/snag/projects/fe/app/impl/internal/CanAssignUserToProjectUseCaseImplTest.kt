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

package cz.adamec.timotej.snag.projects.fe.app.impl.internal

import cz.adamec.timotej.snag.authorization.business.UserRole
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.foundation.common.UuidProvider
import cz.adamec.timotej.snag.core.network.fe.ConnectionStatusProvider
import cz.adamec.timotej.snag.projects.app.model.AppProjectData
import cz.adamec.timotej.snag.projects.fe.app.api.CanAssignUserToProjectUseCase
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectsDb
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import cz.adamec.timotej.snag.users.app.model.AppUserData
import cz.adamec.timotej.snag.users.fe.driven.test.FakeUsersDb
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class CanAssignUserToProjectUseCaseImplTest : FrontendKoinInitializedTest() {
    private val connectionFlow = MutableStateFlow(true)

    private val fakeUsersDb: FakeUsersDb by inject()
    private val fakeProjectsDb: FakeProjectsDb by inject()
    private val useCase: CanAssignUserToProjectUseCase by inject()

    private val currentUserId = Uuid.parse("00000000-0000-0000-0005-000000000001")
    private val projectId = UuidProvider.getUuid()

    override fun additionalKoinModules(): List<Module> =
        listOf(
            module {
                single<ConnectionStatusProvider> {
                    object : ConnectionStatusProvider {
                        override fun isConnectedFlow(): Flow<Boolean> = connectionFlow
                    }
                }
            },
        )

    private fun seedCurrentUser(role: UserRole?) {
        fakeUsersDb.setUser(
            AppUserData(
                id = currentUserId,
                entraId = "entra-id",
                email = "user@example.com",
                role = role,
                updatedAt = Timestamp(100L),
            ),
        )
    }

    private fun seedProject(isClosed: Boolean = false) {
        fakeProjectsDb.setProject(
            AppProjectData(
                id = projectId,
                name = "Test Project",
                address = "Address",
                creatorId = currentUserId,
                isClosed = isClosed,
                updatedAt = Timestamp(100L),
            ),
        )
    }

    @Test
    fun `returns true for administrator on open project`() =
        runTest(testDispatcher) {
            seedCurrentUser(role = UserRole.ADMINISTRATOR)
            seedProject()

            assertTrue(useCase(projectId).first())
        }

    @Test
    fun `returns true for passport lead on open project`() =
        runTest(testDispatcher) {
            seedCurrentUser(role = UserRole.PASSPORT_LEAD)
            seedProject()

            assertTrue(useCase(projectId).first())
        }

    @Test
    fun `returns true for service lead on open project`() =
        runTest(testDispatcher) {
            seedCurrentUser(role = UserRole.SERVICE_LEAD)
            seedProject()

            assertTrue(useCase(projectId).first())
        }

    @Test
    fun `returns false for passport technician`() =
        runTest(testDispatcher) {
            seedCurrentUser(role = UserRole.PASSPORT_TECHNICIAN)
            seedProject()

            assertFalse(useCase(projectId).first())
        }

    @Test
    fun `returns false for service worker`() =
        runTest(testDispatcher) {
            seedCurrentUser(role = UserRole.SERVICE_WORKER)
            seedProject()

            assertFalse(useCase(projectId).first())
        }

    @Test
    fun `returns false for user without role`() =
        runTest(testDispatcher) {
            seedCurrentUser(role = null)
            seedProject()

            assertFalse(useCase(projectId).first())
        }

    @Test
    fun `returns false when project is closed`() =
        runTest(testDispatcher) {
            seedCurrentUser(role = UserRole.ADMINISTRATOR)
            seedProject(isClosed = true)

            assertFalse(useCase(projectId).first())
        }

    @Test
    fun `returns false when user not found`() =
        runTest(testDispatcher) {
            seedProject()

            assertFalse(useCase(projectId).first())
        }

    @Test
    fun `returns false when project not found`() =
        runTest(testDispatcher) {
            seedCurrentUser(role = UserRole.ADMINISTRATOR)

            assertFalse(useCase(projectId).first())
        }

    @Test
    fun `returns false when offline`() =
        runTest(testDispatcher) {
            seedCurrentUser(role = UserRole.ADMINISTRATOR)
            seedProject()
            connectionFlow.value = false

            assertFalse(useCase(projectId).first())
        }
}
