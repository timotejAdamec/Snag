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

import app.cash.turbine.test
import cz.adamec.timotej.snag.projects.fe.app.api.CanEditProjectEntitiesUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.CanModifyProjectFilesUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class NonWebCanModifyProjectFilesUseCaseImplTest {
    private val canEditFlow = MutableStateFlow(true)
    private val projectId = Uuid.parse("00000000-0000-0000-0000-000000000001")

    private val fakeCanEditProjectEntitiesUseCase =
        object : CanEditProjectEntitiesUseCase {
            override fun invoke(projectId: Uuid): Flow<Boolean> = canEditFlow
        }

    private val useCase: CanModifyProjectFilesUseCase =
        NonWebCanModifyProjectFilesUseCaseImpl(
            canEditProjectEntitiesUseCase = fakeCanEditProjectEntitiesUseCase,
        )

    @Test
    fun `returns true when can edit`() =
        runTest {
            canEditFlow.value = true

            useCase(projectId).test {
                assertTrue(awaitItem())
            }
        }

    @Test
    fun `returns false when cannot edit`() =
        runTest {
            canEditFlow.value = false

            useCase(projectId).test {
                assertFalse(awaitItem())
            }
        }
}
