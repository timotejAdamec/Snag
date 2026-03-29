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

package cz.adamec.timotej.snag.users.fe.driving.impl.internal.userManagement.vm

import cz.adamec.timotej.snag.authorization.business.UserRole
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.lib.design.fe.error.UiError
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import cz.adamec.timotej.snag.users.app.model.AppUserData
import cz.adamec.timotej.snag.users.fe.app.api.ChangeUserRoleUseCase
import cz.adamec.timotej.snag.users.fe.app.api.GetAllowedRoleOptionsUseCase
import cz.adamec.timotej.snag.users.fe.app.api.GetUsersUseCase
import cz.adamec.timotej.snag.users.fe.driven.test.FakeUsersApi
import cz.adamec.timotej.snag.users.fe.driven.test.FakeUsersDb
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class UserManagementViewModelTest : FrontendKoinInitializedTest() {
    private val fakeUsersDb: FakeUsersDb by inject()
    private val fakeUsersApi: FakeUsersApi by inject()
    private val getUsersUseCase: GetUsersUseCase by inject()
    private val changeUserRoleUseCase: ChangeUserRoleUseCase by inject()
    private val getAllowedRoleOptionsUseCase: GetAllowedRoleOptionsUseCase by inject()

    private val testUser =
        AppUserData(
            id = Uuid.parse("00000000-0000-0000-0000-000000000001"),
            authProviderId = "entra-1",
            email = "user@example.com",
            role = null,
            updatedAt = Timestamp(100L),
        )

    override fun additionalKoinModules(): List<Module> =
        listOf(
            module {
                factory<GetAllowedRoleOptionsUseCase> {
                    object : GetAllowedRoleOptionsUseCase {
                        override fun invoke(targetCurrentRole: UserRole?) =
                            flowOf(
                                setOf(
                                    UserRole.ADMINISTRATOR,
                                    UserRole.SERVICE_LEAD,
                                    UserRole.SERVICE_WORKER,
                                    UserRole.PASSPORT_LEAD,
                                    UserRole.PASSPORT_TECHNICIAN,
                                ),
                            )
                    }
                }
            },
        )

    private fun createViewModel() =
        UserManagementViewModel(
            getUsersUseCase = getUsersUseCase,
            changeUserRoleUseCase = changeUserRoleUseCase,
            getAllowedRoleOptionsUseCase = getAllowedRoleOptionsUseCase,
        )

    @Test
    fun `loads users into state`() =
        runTest(testDispatcher) {
            fakeUsersDb.setUser(testUser)

            val viewModel = createViewModel()
            val subscriber = launch { viewModel.state.collect { } }
            advanceUntilIdle()

            val state = viewModel.state.value
            assertEquals(false, state.isLoading)
            assertEquals(1, state.users.size)
            assertEquals(testUser.email, state.users[0].email)
            assertEquals(null, state.users[0].role)
            subscriber.cancel()
        }

    @Test
    fun `shows empty list when no users`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            val subscriber = launch { viewModel.state.collect { } }
            advanceUntilIdle()

            val state = viewModel.state.value
            assertEquals(false, state.isLoading)
            assertEquals(0, state.users.size)
            subscriber.cancel()
        }

    @Test
    fun `onRoleChanged calls use case and emits error on failure`() =
        runTest(testDispatcher) {
            fakeUsersDb.setUser(testUser)
            fakeUsersApi.setUser(testUser)

            val viewModel = createViewModel()
            val subscriber = launch { viewModel.state.collect { } }
            advanceUntilIdle()

            fakeUsersApi.forcedFailure = OnlineDataResult.Failure.NetworkUnavailable

            viewModel.onRoleChanged(testUser.id, UserRole.ADMINISTRATOR)
            advanceUntilIdle()

            val error = viewModel.errorsFlow.first()
            assertIs<UiError.NetworkUnavailable>(error)
            subscriber.cancel()
        }

    @Test
    fun `onRoleChanged updates state on success`() =
        runTest(testDispatcher) {
            fakeUsersDb.setUser(testUser)
            fakeUsersApi.setUser(testUser)

            val viewModel = createViewModel()
            val subscriber = launch { viewModel.state.collect { } }
            advanceUntilIdle()

            viewModel.onRoleChanged(testUser.id, UserRole.SERVICE_WORKER)
            advanceUntilIdle()

            val state = viewModel.state.value
            assertEquals(UserRole.SERVICE_WORKER, state.users[0].role)
            subscriber.cancel()
        }

    @Test
    fun `isUpdatingRole is false after successful role change`() =
        runTest(testDispatcher) {
            fakeUsersDb.setUser(testUser)
            fakeUsersApi.setUser(testUser)

            val viewModel = createViewModel()
            val subscriber = launch { viewModel.state.collect { } }
            advanceUntilIdle()

            viewModel.onRoleChanged(testUser.id, UserRole.SERVICE_LEAD)
            advanceUntilIdle()

            val userItem = viewModel.state.value.users[0]
            assertTrue(userItem.isRoleChangeEnabled)
            subscriber.cancel()
        }

    @Test
    fun `isUpdatingRole is false after failed role change`() =
        runTest(testDispatcher) {
            fakeUsersDb.setUser(testUser)
            fakeUsersApi.setUser(testUser)

            val viewModel = createViewModel()
            val subscriber = launch { viewModel.state.collect { } }
            advanceUntilIdle()

            fakeUsersApi.forcedFailure = OnlineDataResult.Failure.NetworkUnavailable

            viewModel.onRoleChanged(testUser.id, UserRole.PASSPORT_LEAD)
            advanceUntilIdle()

            val userItem = viewModel.state.value.users[0]
            assertTrue(userItem.isRoleChangeEnabled)
            subscriber.cancel()
        }
}
