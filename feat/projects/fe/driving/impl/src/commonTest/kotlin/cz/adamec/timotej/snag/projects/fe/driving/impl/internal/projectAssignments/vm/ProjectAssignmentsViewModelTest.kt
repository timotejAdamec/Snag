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

import cz.adamec.timotej.snag.lib.design.fe.error.UiError
import cz.adamec.timotej.snag.projects.fe.app.api.AssignUserToProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.CanAssignUserToProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.GetProjectAssignmentsUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.RemoveUserFromProjectUseCase
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import cz.adamec.timotej.snag.users.fe.app.api.GetUsersUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectAssignmentsViewModelTest : FrontendKoinInitializedTest() {
    private val canManageFlow = MutableStateFlow(true)
    private val assignedCalls = mutableListOf<Pair<Uuid, Uuid>>()
    private val removedCalls = mutableListOf<Pair<Uuid, Uuid>>()

    private val getProjectAssignmentsUseCase: GetProjectAssignmentsUseCase by inject()
    private val getUsersUseCase: GetUsersUseCase by inject()
    private val canAssignUserToProjectUseCase: CanAssignUserToProjectUseCase by inject()
    private val assignUserToProjectUseCase: AssignUserToProjectUseCase by inject()
    private val removeUserFromProjectUseCase: RemoveUserFromProjectUseCase by inject()

    override fun additionalKoinModules(): List<Module> =
        listOf(
            module {
                factory<CanAssignUserToProjectUseCase> {
                    object : CanAssignUserToProjectUseCase {
                        override fun invoke(projectId: Uuid) = canManageFlow
                    }
                }
                factory<AssignUserToProjectUseCase> {
                    object : AssignUserToProjectUseCase {
                        override suspend fun invoke(
                            projectId: Uuid,
                            userId: Uuid,
                        ) {
                            assignedCalls.add(projectId to userId)
                        }
                    }
                }
                factory<RemoveUserFromProjectUseCase> {
                    object : RemoveUserFromProjectUseCase {
                        override suspend fun invoke(
                            projectId: Uuid,
                            userId: Uuid,
                        ) {
                            removedCalls.add(projectId to userId)
                        }
                    }
                }
            },
        )

    private fun createViewModel(projectId: Uuid) =
        ProjectAssignmentsViewModel(
            projectId = projectId,
            getProjectAssignmentsUseCase = getProjectAssignmentsUseCase,
            getUsersUseCase = getUsersUseCase,
            canAssignUserToProjectUseCase = canAssignUserToProjectUseCase,
            assignUserToProjectUseCase = assignUserToProjectUseCase,
            removeUserFromProjectUseCase = removeUserFromProjectUseCase,
        )

    @Test
    fun `onAssignUser when not allowed sends unknown error`() =
        runTest(testDispatcher) {
            val projectId = Uuid.random()
            canManageFlow.value = false

            val viewModel = createViewModel(projectId)
            val subscriber = launch { viewModel.state.collect { } }
            advanceUntilIdle()

            viewModel.onAssignUser(Uuid.random())

            val error = viewModel.errorsFlow.first()
            assertIs<UiError.Unknown>(error)
            assertTrue(assignedCalls.isEmpty())
            subscriber.cancel()
        }

    @Test
    fun `onRemoveUser when not allowed sends unknown error`() =
        runTest(testDispatcher) {
            val projectId = Uuid.random()
            canManageFlow.value = false

            val viewModel = createViewModel(projectId)
            val subscriber = launch { viewModel.state.collect { } }
            advanceUntilIdle()

            viewModel.onRemoveUser(Uuid.random())

            val error = viewModel.errorsFlow.first()
            assertIs<UiError.Unknown>(error)
            assertTrue(removedCalls.isEmpty())
            subscriber.cancel()
        }

    @Test
    fun `onAssignUser when allowed calls use case with correct ids`() =
        runTest(testDispatcher) {
            val projectId = Uuid.random()
            val userId = Uuid.random()

            val viewModel = createViewModel(projectId)
            val subscriber = launch { viewModel.state.collect { } }
            advanceUntilIdle()

            viewModel.onAssignUser(userId)
            advanceUntilIdle()

            assertTrue(assignedCalls.contains(projectId to userId))
            subscriber.cancel()
        }

    @Test
    fun `onRemoveUser when allowed calls use case with correct ids`() =
        runTest(testDispatcher) {
            val projectId = Uuid.random()
            val userId = Uuid.random()

            val viewModel = createViewModel(projectId)
            val subscriber = launch { viewModel.state.collect { } }
            advanceUntilIdle()

            viewModel.onRemoveUser(userId)
            advanceUntilIdle()

            assertTrue(removedCalls.contains(projectId to userId))
            subscriber.cancel()
        }
}
