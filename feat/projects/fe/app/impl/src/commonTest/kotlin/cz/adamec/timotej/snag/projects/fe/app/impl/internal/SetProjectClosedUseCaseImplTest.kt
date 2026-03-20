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

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.foundation.common.UuidProvider
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.projects.app.model.AppProject
import cz.adamec.timotej.snag.projects.app.model.AppProjectData
import cz.adamec.timotej.snag.projects.fe.app.api.SetProjectClosedUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.model.SetProjectClosedRequest
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectsApi
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectsDb
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SetProjectClosedUseCaseImplTest : FrontendKoinInitializedTest() {
    private val fakeProjectsDb: FakeProjectsDb by inject()
    private val fakeProjectsApi: FakeProjectsApi by inject()
    private val useCase: SetProjectClosedUseCase by inject()

    private val projectId = UuidProvider.getUuid()

    private fun seedOpenProject() {
        fakeProjectsDb.setProject(
            AppProjectData(
                id = projectId,
                name = "Test Project",
                address = "Test Address",
                creatorId = UuidProvider.getUuid(),
                updatedAt = Timestamp(100L),
            ),
        )
    }

    private fun seedClosedProject() {
        fakeProjectsDb.setProject(
            AppProjectData(
                id = projectId,
                name = "Test Project",
                address = "Test Address",
                creatorId = UuidProvider.getUuid(),
                isClosed = true,
                updatedAt = Timestamp(100L),
            ),
        )
    }

    @Test
    fun `closes project successfully`() =
        runTest(testDispatcher) {
            seedOpenProject()

            val result = useCase(SetProjectClosedRequest(projectId = projectId, isClosed = true))

            assertIs<OnlineDataResult.Success<Unit>>(result)
            val dbResult = fakeProjectsDb.getProject(projectId)
            assertIs<OfflineFirstDataResult.Success<AppProject?>>(dbResult)
            assertTrue(dbResult.data!!.isClosed)
        }

    @Test
    fun `reopens project successfully`() =
        runTest(testDispatcher) {
            seedClosedProject()

            val result = useCase(SetProjectClosedRequest(projectId = projectId, isClosed = false))

            assertIs<OnlineDataResult.Success<Unit>>(result)
            val dbResult = fakeProjectsDb.getProject(projectId)
            assertIs<OfflineFirstDataResult.Success<AppProject?>>(dbResult)
            assertFalse(dbResult.data!!.isClosed)
        }

    @Test
    fun `propagates API failure`() =
        runTest(testDispatcher) {
            seedOpenProject()
            fakeProjectsApi.forcedFailure = OnlineDataResult.Failure.NetworkUnavailable

            val result = useCase(SetProjectClosedRequest(projectId = projectId, isClosed = true))

            assertIs<OnlineDataResult.Failure.NetworkUnavailable>(result)
            val dbResult = fakeProjectsDb.getProject(projectId)
            assertIs<OfflineFirstDataResult.Success<AppProject?>>(dbResult)
            assertFalse(dbResult.data!!.isClosed)
        }

    @Test
    fun `returns ProgrammerError when project not found`() =
        runTest(testDispatcher) {
            val result = useCase(SetProjectClosedRequest(projectId = projectId, isClosed = true))

            assertIs<OnlineDataResult.Failure.ProgrammerError>(result)
        }
}
