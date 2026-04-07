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
import cz.adamec.timotej.snag.core.network.fe.ConnectionStatusProvider
import cz.adamec.timotej.snag.projects.fe.app.api.CanEditProjectEntitiesUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.CanModifyProjectFilesUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class WebCanModifyProjectFilesUseCaseImplTest {
    private val isConnectedFlow = MutableStateFlow(true)
    private val canEditFlow = MutableStateFlow(true)
    private val projectId = Uuid.parse("00000000-0000-0000-0000-000000000001")

    private val fakeConnectionStatusProvider =
        object : ConnectionStatusProvider {
            override fun isConnectedFlow(): Flow<Boolean> = isConnectedFlow
        }

    private val fakeCanEditProjectEntitiesUseCase =
        object : CanEditProjectEntitiesUseCase {
            override fun invoke(projectId: Uuid): Flow<Boolean> = canEditFlow
        }

    private val useCase: CanModifyProjectFilesUseCase =
        WebCanModifyProjectFilesUseCaseImpl(
            connectionStatusProvider = fakeConnectionStatusProvider,
            canEditProjectEntitiesUseCase = fakeCanEditProjectEntitiesUseCase,
        )

    @Test
    fun `returns true when connected and can edit`() =
        runTest {
            isConnectedFlow.value = true
            canEditFlow.value = true

            useCase(projectId).test {
                assertTrue(awaitItem())
            }
        }

    @Test
    fun `returns false when disconnected`() =
        runTest {
            isConnectedFlow.value = false
            canEditFlow.value = true

            useCase(projectId).test {
                assertFalse(awaitItem())
            }
        }

    @Test
    fun `returns false when cannot edit`() =
        runTest {
            isConnectedFlow.value = true
            canEditFlow.value = false

            useCase(projectId).test {
                assertFalse(awaitItem())
            }
        }

    @Test
    fun `reacts to connection status changes`() =
        runTest {
            isConnectedFlow.value = true
            canEditFlow.value = true

            useCase(projectId).test {
                assertTrue(awaitItem())

                isConnectedFlow.value = false
                assertFalse(awaitItem())

                isConnectedFlow.value = true
                assertTrue(awaitItem())
            }
        }
}
