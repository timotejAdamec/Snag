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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CanModifyFindingPhotosUseCaseImplTest {
    private val connectionFlow = MutableStateFlow(true)

    private val fakeConnectionStatusProvider =
        object : ConnectionStatusProvider {
            override fun isConnectedFlow(): Flow<Boolean> = connectionFlow
        }

    private val useCase: CanModifyFindingPhotosUseCase =
        CanModifyFindingPhotosUseCaseImpl(
            connectionStatusProvider = fakeConnectionStatusProvider,
        )

    @Test
    fun `returns true when connected`() =
        runTest {
            connectionFlow.value = true

            useCase().test {
                assertTrue(awaitItem())
            }
        }

    @Test
    fun `returns false when disconnected`() =
        runTest {
            connectionFlow.value = false

            useCase().test {
                assertFalse(awaitItem())
            }
        }

    @Test
    fun `emits updated value when connectivity changes`() =
        runTest {
            connectionFlow.value = true

            useCase().test {
                assertTrue(awaitItem())

                connectionFlow.value = false
                assertFalse(awaitItem())

                connectionFlow.value = true
                assertTrue(awaitItem())
            }
        }

    @Test
    fun `does not emit duplicate values`() =
        runTest {
            connectionFlow.value = true

            useCase().test {
                assertTrue(awaitItem())

                connectionFlow.value = true // same value
                expectNoEvents()

                connectionFlow.value = false
                assertFalse(awaitItem())
            }
        }
}
