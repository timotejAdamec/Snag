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
import cz.adamec.timotej.snag.projects.fe.app.api.GetProjectAssignmentsUseCase
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
class GetProjectAssignmentsUseCaseImplTest : FrontendKoinInitializedTest() {
    private val fakeProjectAssignmentsDb: FakeProjectAssignmentsDb by inject()
    private val fakeProjectsApi: FakeProjectsApi by inject()
    private val useCase: GetProjectAssignmentsUseCase by inject()

    private val projectId = UuidProvider.getUuid()

    @Test
    fun `returns empty set when no assignments exist`() =
        runTest(testDispatcher) {
            val result = useCase(projectId).first()

            assertIs<OfflineFirstDataResult.Success<Set<*>>>(result)
            assertTrue { result.data.isEmpty() }
        }

    @Test
    fun `returns locally stored assignments`() =
        runTest(testDispatcher) {
            val userId1 = UuidProvider.getUuid()
            val userId2 = UuidProvider.getUuid()
            fakeProjectAssignmentsDb.setAssignments(projectId, setOf(userId1, userId2))

            val result = useCase(projectId).first()

            assertIs<OfflineFirstDataResult.Success<Set<*>>>(result)
            assertEquals(setOf(userId1, userId2), result.data)
        }

    @Test
    fun `does not fail when API is unavailable`() =
        runTest(testDispatcher) {
            fakeProjectsApi.forcedFailure = OnlineDataResult.Failure.NetworkUnavailable

            val result = useCase(projectId).first()

            assertIs<OfflineFirstDataResult.Success<Set<*>>>(result)
            assertTrue { result.data.isEmpty() }
        }
}
