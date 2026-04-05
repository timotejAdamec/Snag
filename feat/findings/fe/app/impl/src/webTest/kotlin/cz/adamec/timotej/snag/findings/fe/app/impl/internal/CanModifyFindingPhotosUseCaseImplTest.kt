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

package cz.adamec.timotej.snag.findings.fe.app.impl.internal

import app.cash.turbine.test
import cz.adamec.timotej.snag.findings.fe.app.api.CanModifyFindingPhotosUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.CanModifyProjectFilesUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class CanModifyFindingPhotosUseCaseImplTest {
    private val canModifyFlow = MutableStateFlow(true)
    private val projectId = Uuid.parse("00000000-0000-0000-0000-000000000001")

    private val fakeCanModifyProjectFilesUseCase =
        object : CanModifyProjectFilesUseCase {
            override fun invoke(projectId: Uuid): Flow<Boolean> = canModifyFlow
        }

    private val useCase: CanModifyFindingPhotosUseCase =
        CanModifyFindingPhotosUseCaseImpl(
            canModifyProjectFilesUseCase = fakeCanModifyProjectFilesUseCase,
        )

    @Test
    fun `delegates to canModifyProjectFilesUseCase and returns true`() =
        runTest {
            canModifyFlow.value = true

            useCase(projectId).test {
                assertTrue(awaitItem())
            }
        }

    @Test
    fun `delegates to canModifyProjectFilesUseCase and returns false`() =
        runTest {
            canModifyFlow.value = false

            useCase(projectId).test {
                assertFalse(awaitItem())
            }
        }

    @Test
    fun `emits updated value when delegate changes`() =
        runTest {
            canModifyFlow.value = true

            useCase(projectId).test {
                assertTrue(awaitItem())

                canModifyFlow.value = false
                assertFalse(awaitItem())

                canModifyFlow.value = true
                assertTrue(awaitItem())
            }
        }
}
