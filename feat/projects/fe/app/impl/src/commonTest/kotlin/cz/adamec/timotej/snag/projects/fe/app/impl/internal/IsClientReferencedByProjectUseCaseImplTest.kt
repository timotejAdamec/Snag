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
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.projects.app.model.AppProjectData
import cz.adamec.timotej.snag.projects.fe.app.api.IsClientReferencedByProjectUseCase
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectsDb
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class IsClientReferencedByProjectUseCaseImplTest : FrontendKoinInitializedTest() {
    private val fakeProjectsDb: FakeProjectsDb by inject()
    private val useCase: IsClientReferencedByProjectUseCase by inject()

    private val clientId = Uuid.parse("00000000-0000-0000-0000-000000000001")

    @Test
    fun `returns false when no project references the client`() =
        runTest(testDispatcher) {
            val result = useCase(clientId)

            assertIs<OfflineFirstDataResult.Success<Boolean>>(result)
            assertFalse(result.data)
        }

    @Test
    fun `returns true when a project references the client`() =
        runTest(testDispatcher) {
            fakeProjectsDb.setProject(
                AppProjectData(
                    id = Uuid.random(),
                    name = "Test Project",
                    address = "Test Address",
                    clientId = clientId,
                    creatorId = Uuid.random(),
                    updatedAt = Timestamp(100L),
                ),
            )

            val result = useCase(clientId)

            assertIs<OfflineFirstDataResult.Success<Boolean>>(result)
            assertTrue(result.data)
        }
}
