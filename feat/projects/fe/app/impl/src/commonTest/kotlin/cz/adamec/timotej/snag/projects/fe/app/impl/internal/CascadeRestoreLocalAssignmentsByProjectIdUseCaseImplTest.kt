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
import cz.adamec.timotej.snag.projects.fe.app.api.CascadeRestoreLocalAssignmentsByProjectIdUseCase
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CascadeRestoreLocalAssignmentsByProjectIdUseCaseImplTest : FrontendKoinInitializedTest() {
    private val fakeProjectAssignmentsDb: FakeProjectAssignmentsDb by inject()
    private val fakeProjectsApi: FakeProjectsApi by inject()
    private val useCase: CascadeRestoreLocalAssignmentsByProjectIdUseCase by inject()

    private val projectId = UuidProvider.getUuid()

    @Test
    fun `restores assignments from API`() =
        runTest(testDispatcher) {
            val userId1 = UuidProvider.getUuid()
            val userId2 = UuidProvider.getUuid()
            fakeProjectsApi.projectAssignments[projectId] = setOf(userId1, userId2)

            useCase(projectId)

            val result = fakeProjectAssignmentsDb.getAssignedUserIdsFlow(projectId).first()
            assertIs<OfflineFirstDataResult.Success<Set<*>>>(result)
            assertEquals(setOf(userId1, userId2), result.data)
        }

    @Test
    fun `handles API failure gracefully`() =
        runTest(testDispatcher) {
            fakeProjectsApi.forcedFailure = OnlineDataResult.Failure.NetworkUnavailable

            useCase(projectId)

            val result = fakeProjectAssignmentsDb.getAssignedUserIdsFlow(projectId).first()
            assertIs<OfflineFirstDataResult.Success<Set<*>>>(result)
            assertTrue { result.data.isEmpty() }
        }
}
