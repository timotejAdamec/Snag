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
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.sync.ProjectAssignmentsPullSyncHandler
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectAssignmentsDb
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectsApi
import cz.adamec.timotej.snag.sync.fe.app.api.handler.PullSyncOperationResult
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectAssignmentsPullSyncHandlerTest : FrontendKoinInitializedTest() {
    private val fakeProjectsApi: FakeProjectsApi by inject()
    private val fakeProjectAssignmentsDb: FakeProjectAssignmentsDb by inject()
    private val handler: ProjectAssignmentsPullSyncHandler by inject()

    private val projectId = UuidProvider.getUuid()

    @Test
    fun `fetches assignments from API and replaces in DB`() =
        runTest(testDispatcher) {
            val userId1 = UuidProvider.getUuid()
            val userId2 = UuidProvider.getUuid()
            fakeProjectsApi.projectAssignments[projectId] = setOf(userId1, userId2)

            val result = handler.execute(scopeId = projectId)

            assertEquals(PullSyncOperationResult.Success, result)
            val dbResult = fakeProjectAssignmentsDb.getAssignedUserIdsFlow(projectId).first()
            assertIs<OfflineFirstDataResult.Success<Set<*>>>(dbResult)
            assertEquals(setOf(userId1, userId2), dbResult.data)
        }

    @Test
    fun `returns failure when API fails`() =
        runTest(testDispatcher) {
            fakeProjectsApi.forcedFailure = OnlineDataResult.Failure.NetworkUnavailable

            val result = handler.execute(scopeId = projectId)

            assertEquals(PullSyncOperationResult.Failure, result)
        }

    @Test
    fun `replaces existing assignments with new set`() =
        runTest(testDispatcher) {
            val oldUserId = UuidProvider.getUuid()
            val newUserId = UuidProvider.getUuid()
            fakeProjectAssignmentsDb.setAssignments(projectId, setOf(oldUserId))
            fakeProjectsApi.projectAssignments[projectId] = setOf(newUserId)

            handler.execute(scopeId = projectId)

            val dbResult = fakeProjectAssignmentsDb.getAssignedUserIdsFlow(projectId).first()
            assertIs<OfflineFirstDataResult.Success<Set<*>>>(dbResult)
            assertEquals(setOf(newUserId), dbResult.data)
        }
}
