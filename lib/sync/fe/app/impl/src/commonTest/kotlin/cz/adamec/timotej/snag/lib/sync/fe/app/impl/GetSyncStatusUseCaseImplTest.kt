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

package cz.adamec.timotej.snag.lib.sync.fe.app.impl

import app.cash.turbine.test
import cz.adamec.timotej.snag.lib.sync.fe.app.api.SyncEngineStatus
import cz.adamec.timotej.snag.lib.sync.fe.app.api.SyncStatus
import cz.adamec.timotej.snag.lib.sync.fe.app.impl.internal.GetSyncStatusUseCaseImpl
import cz.adamec.timotej.snag.lib.sync.fe.driven.test.FakeGetSyncEngineStatusUseCase
import cz.adamec.timotej.snag.network.fe.InternetConnectionStatusListener
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetSyncStatusUseCaseImplTest {
    private val fakeEngineStatus = FakeGetSyncEngineStatusUseCase()
    private val fakeConnectionListener = FakeInternetConnectionStatusListener()

    private val useCase =
        GetSyncStatusUseCaseImpl(
            getSyncEngineStatus = fakeEngineStatus,
            connectionStatusListener = fakeConnectionListener,
        )

    @Test
    fun `connected and Idle yields Synced`() =
        runTest {
            fakeConnectionListener.emit(true)
            fakeEngineStatus.emit(SyncEngineStatus.Idle)

            useCase().test {
                assertEquals(SyncStatus.Synced, awaitItem())
            }
        }

    @Test
    fun `connected and Syncing yields Syncing`() =
        runTest {
            fakeConnectionListener.emit(true)
            fakeEngineStatus.emit(SyncEngineStatus.Syncing)

            useCase().test {
                assertEquals(SyncStatus.Syncing, awaitItem())
            }
        }

    @Test
    fun `connected and Failed yields Error`() =
        runTest {
            fakeConnectionListener.emit(true)
            fakeEngineStatus.emit(SyncEngineStatus.Failed(pendingCount = 3))

            useCase().test {
                assertEquals(SyncStatus.Error, awaitItem())
            }
        }

    @Test
    fun `disconnected and Idle yields Offline`() =
        runTest {
            fakeConnectionListener.emit(false)
            fakeEngineStatus.emit(SyncEngineStatus.Idle)

            useCase().test {
                assertEquals(SyncStatus.Offline, awaitItem())
            }
        }

    @Test
    fun `disconnected and Syncing yields Offline`() =
        runTest {
            fakeConnectionListener.emit(false)
            fakeEngineStatus.emit(SyncEngineStatus.Syncing)

            useCase().test {
                assertEquals(SyncStatus.Offline, awaitItem())
            }
        }

    @Test
    fun `disconnected and Failed yields Offline`() =
        runTest {
            fakeConnectionListener.emit(false)
            fakeEngineStatus.emit(SyncEngineStatus.Failed(pendingCount = 2))

            useCase().test {
                assertEquals(SyncStatus.Offline, awaitItem())
            }
        }

    private class FakeInternetConnectionStatusListener : InternetConnectionStatusListener {
        private val connectedFlow = MutableStateFlow(true)

        fun emit(connected: Boolean) {
            connectedFlow.value = connected
        }

        override fun isConnectedFlow(): Flow<Boolean> = connectedFlow
    }
}
