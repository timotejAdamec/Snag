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
import cz.adamec.timotej.snag.projects.app.model.AppProjectData
import cz.adamec.timotej.snag.projects.fe.app.api.CanEditProjectEntitiesUseCase
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectsDb
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CanEditProjectEntitiesUseCaseImplTest : FrontendKoinInitializedTest() {
    private val fakeProjectsDb: FakeProjectsDb by inject()
    private val useCase: CanEditProjectEntitiesUseCase by inject()

    private val projectId = UuidProvider.getUuid()

    @Test
    fun `returns true for open project`() =
        runTest(testDispatcher) {
            fakeProjectsDb.setProject(
                AppProjectData(
                    id = projectId,
                    name = "Open Project",
                    address = "Address",
                    isClosed = false,
                    updatedAt = Timestamp(100L),
                ),
            )

            val result = useCase(projectId).first()

            assertTrue(result)
        }

    @Test
    fun `returns false for closed project`() =
        runTest(testDispatcher) {
            fakeProjectsDb.setProject(
                AppProjectData(
                    id = projectId,
                    name = "Closed Project",
                    address = "Address",
                    isClosed = true,
                    updatedAt = Timestamp(100L),
                ),
            )

            val result = useCase(projectId).first()

            assertFalse(result)
        }

    @Test
    fun `returns true when project not found`() =
        runTest(testDispatcher) {
            val result = useCase(projectId).first()

            assertTrue(result)
        }

    @Test
    fun `returns true on ProgrammerError`() =
        runTest(testDispatcher) {
            fakeProjectsDb.forcedFailure =
                OfflineFirstDataResult.ProgrammerError(RuntimeException("DB error"))

            val result = useCase(projectId).first()

            assertTrue(result)
        }
}
