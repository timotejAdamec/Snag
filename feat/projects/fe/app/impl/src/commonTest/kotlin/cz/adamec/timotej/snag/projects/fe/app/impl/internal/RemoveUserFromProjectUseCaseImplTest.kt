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

import cz.adamec.timotej.snag.core.foundation.common.UuidProvider
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.projects.fe.app.api.RemoveUserFromProjectUseCase
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectAssignmentsDb
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectsApi
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class RemoveUserFromProjectUseCaseImplTest : FrontendKoinInitializedTest() {
    private val fakeProjectsApi: FakeProjectsApi by inject()
    private val fakeProjectAssignmentsDb: FakeProjectAssignmentsDb by inject()
    private val useCase: RemoveUserFromProjectUseCase by inject()

    private val projectId = UuidProvider.getUuid()
    private val userId = UuidProvider.getUuid()

    @Test
    fun `removes user via API and refreshes local DB`() =
        runTest(testDispatcher) {
            val otherUserId = UuidProvider.getUuid()
            fakeProjectsApi.projectAssignments[projectId] = setOf(otherUserId)

            useCase(
                projectId = projectId,
                userId = userId,
            )

            val result = fakeProjectAssignmentsDb.getAssignedUserIdsFlow(projectId).first()
            assertIs<OfflineFirstDataResult.Success<Set<*>>>(result)
            assertEquals(setOf(otherUserId), result.data)
        }

    @Test
    fun `local DB reflects API state after remove`() =
        runTest(testDispatcher) {
            fakeProjectsApi.projectAssignments[projectId] = emptySet()

            useCase(
                projectId = projectId,
                userId = userId,
            )

            val result = fakeProjectAssignmentsDb.getAssignedUserIdsFlow(projectId).first()
            assertIs<OfflineFirstDataResult.Success<Set<*>>>(result)
            assertEquals(emptySet<Any>(), result.data)
        }

    @Test
    fun `does not throw on API failure`() =
        runTest(testDispatcher) {
            fakeProjectsApi.forcedFailure = OnlineDataResult.Failure.NetworkUnavailable

            useCase(
                projectId = projectId,
                userId = userId,
            )

            val result = fakeProjectAssignmentsDb.getAssignedUserIdsFlow(projectId).first()
            assertIs<OfflineFirstDataResult.Success<Set<*>>>(result)
            assertEquals(emptySet<Any>(), result.data)
        }
}
