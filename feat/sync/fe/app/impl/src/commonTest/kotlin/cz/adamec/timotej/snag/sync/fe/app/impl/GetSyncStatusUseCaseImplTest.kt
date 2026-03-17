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

package cz.adamec.timotej.snag.sync.fe.app.impl

import app.cash.turbine.test
import cz.adamec.timotej.snag.core.foundation.common.ApplicationScope
import cz.adamec.timotej.snag.core.network.fe.ConnectionStatusProvider
import cz.adamec.timotej.snag.sync.fe.app.api.handler.SyncOperationHandler
import cz.adamec.timotej.snag.sync.fe.app.api.handler.SyncOperationResult
import cz.adamec.timotej.snag.sync.fe.app.impl.internal.GetSyncStatusUseCaseImpl
import cz.adamec.timotej.snag.sync.fe.app.impl.internal.PullSyncTrackerImpl
import cz.adamec.timotej.snag.sync.fe.app.impl.internal.SyncEngine
import cz.adamec.timotej.snag.sync.fe.driven.test.FakeSyncQueue
import cz.adamec.timotej.snag.sync.fe.model.SyncOperationType
import cz.adamec.timotej.snag.sync.fe.model.SyncStatus
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class GetSyncStatusUseCaseImplTest : FrontendKoinInitializedTest() {
    private val fakeSyncQueue: FakeSyncQueue by inject()
    private val applicationScope: ApplicationScope by inject()
    private val fakeConnectionStatusProvider = FakeConnectionStatusProvider()
    private val pullSyncTracker = PullSyncTrackerImpl()

    private fun createEngine(handlers: List<SyncOperationHandler> = emptyList()) =
        SyncEngine(
            syncQueue = fakeSyncQueue,
            handlers = handlers,
            applicationScope = applicationScope,
        )

    private fun createUseCase(engine: SyncEngine) =
        GetSyncStatusUseCaseImpl(
            syncEngine = engine,
            connectionStatusProvider = fakeConnectionStatusProvider,
            pullSyncTracker = pullSyncTracker,
        )

    @Test
    fun `connected and Idle yields Synced`() =
        runTest(testDispatcher) {
            val engine = createEngine()
            val useCase = createUseCase(engine)
            fakeConnectionStatusProvider.emit(true)

            useCase().test {
                assertEquals(SyncStatus.Synced, awaitItem())
            }
        }

    @Test
    fun `connected and Syncing yields Syncing`() =
        runTest(testDispatcher) {
            val deferred = CompletableDeferred<SyncOperationResult>()
            val handler = SuspendingHandler("project", deferred)
            val engine = createEngine(listOf(handler))
            val useCase = createUseCase(engine)
            fakeConnectionStatusProvider.emit(true)

            engine.invoke("project", Uuid.random(), SyncOperationType.UPSERT)
            advanceUntilIdle()

            useCase().test {
                assertEquals(SyncStatus.Syncing, awaitItem())
            }

            deferred.complete(SyncOperationResult.Success)
            advanceUntilIdle()
        }

    @Test
    fun `connected and Failed yields Error`() =
        runTest(testDispatcher) {
            val handler = FixedResultHandler("project", SyncOperationResult.Failure)
            val engine = createEngine(listOf(handler))
            val useCase = createUseCase(engine)
            fakeConnectionStatusProvider.emit(true)

            engine.invoke("project", Uuid.random(), SyncOperationType.UPSERT)
            advanceUntilIdle()

            useCase().test {
                assertEquals(SyncStatus.Error, awaitItem())
            }
        }

    @Test
    fun `disconnected and Idle yields Offline`() =
        runTest(testDispatcher) {
            val engine = createEngine()
            val useCase = createUseCase(engine)
            fakeConnectionStatusProvider.emit(false)

            useCase().test {
                assertEquals(SyncStatus.Offline, awaitItem())
            }
        }

    @Test
    fun `disconnected and Syncing yields Offline`() =
        runTest(testDispatcher) {
            val deferred = CompletableDeferred<SyncOperationResult>()
            val handler = SuspendingHandler("project", deferred)
            val engine = createEngine(listOf(handler))
            val useCase = createUseCase(engine)
            fakeConnectionStatusProvider.emit(false)

            engine.invoke("project", Uuid.random(), SyncOperationType.UPSERT)
            advanceUntilIdle()

            useCase().test {
                assertEquals(SyncStatus.Offline, awaitItem())
            }

            deferred.complete(SyncOperationResult.Success)
            advanceUntilIdle()
        }

    @Test
    fun `disconnected and Failed yields Offline`() =
        runTest(testDispatcher) {
            val handler = FixedResultHandler("project", SyncOperationResult.Failure)
            val engine = createEngine(listOf(handler))
            val useCase = createUseCase(engine)
            fakeConnectionStatusProvider.emit(false)

            engine.invoke("project", Uuid.random(), SyncOperationType.UPSERT)
            advanceUntilIdle()

            useCase().test {
                assertEquals(SyncStatus.Offline, awaitItem())
            }
        }

    @Test
    fun `connected and pulling yields Syncing`() =
        runTest(testDispatcher) {
            val engine = createEngine()
            val useCase = createUseCase(engine)
            fakeConnectionStatusProvider.emit(true)

            val deferred = CompletableDeferred<Unit>()
            val job =
                launch {
                    pullSyncTracker.track { deferred.await() }
                }
            advanceUntilIdle()

            useCase().test {
                assertEquals(SyncStatus.Syncing, awaitItem())
            }

            deferred.complete(Unit)
            advanceUntilIdle()
            job.join()
        }

    @Test
    fun `disconnected and pulling yields Offline`() =
        runTest(testDispatcher) {
            val engine = createEngine()
            val useCase = createUseCase(engine)
            fakeConnectionStatusProvider.emit(false)

            val deferred = CompletableDeferred<Unit>()
            val job =
                launch {
                    pullSyncTracker.track { deferred.await() }
                }
            advanceUntilIdle()

            useCase().test {
                assertEquals(SyncStatus.Offline, awaitItem())
            }

            deferred.complete(Unit)
            advanceUntilIdle()
            job.join()
        }

    private class FakeConnectionStatusProvider : ConnectionStatusProvider {
        private val connectedFlow = MutableStateFlow(true)

        fun emit(connected: Boolean) {
            connectedFlow.value = connected
        }

        override fun isConnectedFlow(): Flow<Boolean> = connectedFlow
    }

    private class FixedResultHandler(
        override val entityTypeId: String,
        private val result: SyncOperationResult,
    ) : SyncOperationHandler {
        override suspend fun execute(
            entityId: Uuid,
            operationType: SyncOperationType,
        ): SyncOperationResult = result
    }

    private class SuspendingHandler(
        override val entityTypeId: String,
        private val deferred: CompletableDeferred<SyncOperationResult>,
    ) : SyncOperationHandler {
        override suspend fun execute(
            entityId: Uuid,
            operationType: SyncOperationType,
        ): SyncOperationResult = deferred.await()
    }
}
