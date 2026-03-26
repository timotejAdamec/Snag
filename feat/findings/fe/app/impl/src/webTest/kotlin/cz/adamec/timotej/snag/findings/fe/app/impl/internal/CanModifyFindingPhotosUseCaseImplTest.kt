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
import cz.adamec.timotej.snag.core.network.fe.ConnectionStatusProvider
import cz.adamec.timotej.snag.findings.fe.app.api.CanModifyFindingPhotosUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.CanEditProjectEntitiesUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class CanModifyFindingPhotosUseCaseImplTest {
    private val connectionFlow = MutableStateFlow(true)
    private val canEditFlow = MutableStateFlow(true)
    private val projectId = Uuid.parse("00000000-0000-0000-0000-000000000001")

    private val fakeConnectionStatusProvider =
        object : ConnectionStatusProvider {
            override fun isConnectedFlow(): Flow<Boolean> = connectionFlow
        }

    private val fakeCanEditProjectEntitiesUseCase =
        object : CanEditProjectEntitiesUseCase {
            override fun invoke(projectId: Uuid): Flow<Boolean> = canEditFlow
        }

    private val useCase: CanModifyFindingPhotosUseCase =
        CanModifyFindingPhotosUseCaseImpl(
            connectionStatusProvider = fakeConnectionStatusProvider,
            canEditProjectEntitiesUseCase = fakeCanEditProjectEntitiesUseCase,
        )

    @Test
    fun `returns true when connected and can edit`() =
        runTest {
            connectionFlow.value = true
            canEditFlow.value = true

            useCase(projectId).test {
                assertTrue(awaitItem())
            }
        }

    @Test
    fun `returns false when disconnected`() =
        runTest {
            connectionFlow.value = false
            canEditFlow.value = true

            useCase(projectId).test {
                assertFalse(awaitItem())
            }
        }

    @Test
    fun `returns false when project is not editable`() =
        runTest {
            connectionFlow.value = true
            canEditFlow.value = false

            useCase(projectId).test {
                assertFalse(awaitItem())
            }
        }

    @Test
    fun `returns false when both disconnected and not editable`() =
        runTest {
            connectionFlow.value = false
            canEditFlow.value = false

            useCase(projectId).test {
                assertFalse(awaitItem())
            }
        }

    @Test
    fun `emits updated value when connectivity changes`() =
        runTest {
            connectionFlow.value = true
            canEditFlow.value = true

            useCase(projectId).test {
                assertTrue(awaitItem())

                connectionFlow.value = false
                assertFalse(awaitItem())

                connectionFlow.value = true
                assertTrue(awaitItem())
            }
        }

    @Test
    fun `emits updated value when edit permission changes`() =
        runTest {
            connectionFlow.value = true
            canEditFlow.value = true

            useCase(projectId).test {
                assertTrue(awaitItem())

                canEditFlow.value = false
                assertFalse(awaitItem())

                canEditFlow.value = true
                assertTrue(awaitItem())
            }
        }

    @Test
    fun `does not emit duplicate values`() =
        runTest {
            connectionFlow.value = true
            canEditFlow.value = true

            useCase(projectId).test {
                assertTrue(awaitItem())

                connectionFlow.value = true // same
                expectNoEvents()

                connectionFlow.value = false
                assertFalse(awaitItem())
            }
        }
}
