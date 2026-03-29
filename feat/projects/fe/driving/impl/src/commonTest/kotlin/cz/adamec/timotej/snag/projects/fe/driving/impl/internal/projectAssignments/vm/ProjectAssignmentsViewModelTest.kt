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

package cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projectAssignments.vm

import cz.adamec.timotej.snag.authorization.business.UserRole
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.foundation.common.UuidProvider
import cz.adamec.timotej.snag.lib.design.fe.error.UiError
import cz.adamec.timotej.snag.network.fe.test.FakeConnectionStatusProvider
import cz.adamec.timotej.snag.projects.app.model.AppProjectData
import cz.adamec.timotej.snag.projects.fe.app.api.AssignUserToProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.CanAssignUserToProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.GetProjectAssignmentsUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.RemoveUserFromProjectUseCase
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectsApi
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectsDb
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import cz.adamec.timotej.snag.users.app.model.AppUserData
import cz.adamec.timotej.snag.users.fe.app.api.GetUsersUseCase
import cz.adamec.timotej.snag.users.fe.driven.test.FakeUsersDb
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectAssignmentsViewModelTest : FrontendKoinInitializedTest() {
    private val currentUserId = Uuid.parse("00000000-0000-0000-0005-000000000001")

    private val fakeUsersDb: FakeUsersDb by inject()
    private val fakeProjectsDb: FakeProjectsDb by inject()
    private val fakeProjectsApi: FakeProjectsApi by inject()
    private val fakeConnectionStatusProvider: FakeConnectionStatusProvider by inject()

    private val getProjectAssignmentsUseCase: GetProjectAssignmentsUseCase by inject()
    private val getUsersUseCase: GetUsersUseCase by inject()
    private val canAssignUserToProjectUseCase: CanAssignUserToProjectUseCase by inject()
    private val assignUserToProjectUseCase: AssignUserToProjectUseCase by inject()
    private val removeUserFromProjectUseCase: RemoveUserFromProjectUseCase by inject()

    private fun createViewModel(projectId: Uuid) =
        ProjectAssignmentsViewModel(
            projectId = projectId,
            getProjectAssignmentsUseCase = getProjectAssignmentsUseCase,
            getUsersUseCase = getUsersUseCase,
            canAssignUserToProjectUseCase = canAssignUserToProjectUseCase,
            assignUserToProjectUseCase = assignUserToProjectUseCase,
            removeUserFromProjectUseCase = removeUserFromProjectUseCase,
        )

    private fun seedCurrentUser(role: UserRole? = UserRole.ADMINISTRATOR) {
        fakeUsersDb.setUser(
            AppUserData(
                id = currentUserId,
                entraId = "entra-id",
                email = "admin@test.com",
                role = role,
                updatedAt = Timestamp(0L),
            ),
        )
    }

    private fun seedProject(projectId: Uuid) {
        fakeProjectsDb.setProject(
            AppProjectData(
                id = projectId,
                name = "Test Project",
                address = "Test Address",
                creatorId = UuidProvider.getUuid(),
                updatedAt = Timestamp(10L),
            ),
        )
    }

    @Test
    fun `onAssignUser when not allowed sends unknown error`() =
        runTest(testDispatcher) {
            val projectId = Uuid.random()
            seedCurrentUser()
            seedProject(projectId)
            fakeConnectionStatusProvider.setConnected(false)

            val viewModel = createViewModel(projectId)
            val subscriber = launch { viewModel.state.collect { } }
            advanceUntilIdle()

            viewModel.onAssignUser(Uuid.random())

            val error = viewModel.errorsFlow.first()
            assertIs<UiError.Unknown>(error)
            subscriber.cancel()
        }

    @Test
    fun `onRemoveUser when not allowed sends unknown error`() =
        runTest(testDispatcher) {
            val projectId = Uuid.random()
            seedCurrentUser()
            seedProject(projectId)
            fakeConnectionStatusProvider.setConnected(false)

            val viewModel = createViewModel(projectId)
            val subscriber = launch { viewModel.state.collect { } }
            advanceUntilIdle()

            viewModel.onRemoveUser(Uuid.random())

            val error = viewModel.errorsFlow.first()
            assertIs<UiError.Unknown>(error)
            subscriber.cancel()
        }

    @Test
    fun `onAssignUser when allowed calls use case with correct ids`() =
        runTest(testDispatcher) {
            val projectId = Uuid.random()
            val userId = Uuid.random()
            seedCurrentUser()
            seedProject(projectId)

            val viewModel = createViewModel(projectId)
            val subscriber = launch { viewModel.state.collect { } }
            advanceUntilIdle()

            viewModel.onAssignUser(userId)
            advanceUntilIdle()

            assertTrue(fakeProjectsApi.projectAssignments[projectId]?.contains(userId) == true)
            subscriber.cancel()
        }

    @Test
    fun `onRemoveUser when allowed calls use case with correct ids`() =
        runTest(testDispatcher) {
            val projectId = Uuid.random()
            val userId = Uuid.random()
            seedCurrentUser()
            seedProject(projectId)
            fakeProjectsApi.projectAssignments[projectId] = setOf(userId)

            val viewModel = createViewModel(projectId)
            val subscriber = launch { viewModel.state.collect { } }
            advanceUntilIdle()

            viewModel.onRemoveUser(userId)
            advanceUntilIdle()

            assertTrue(fakeProjectsApi.projectAssignments[projectId]?.contains(userId) != true)
            subscriber.cancel()
        }
}
