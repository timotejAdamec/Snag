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
import cz.adamec.timotej.snag.sync.fe.app.api.handler.PullSyncOperationHandler
import cz.adamec.timotej.snag.sync.fe.app.api.handler.PullSyncOperationResult
import cz.adamec.timotej.snag.sync.fe.app.api.handler.PushSyncOperationHandler
import cz.adamec.timotej.snag.sync.fe.app.api.handler.PushSyncOperationResult
import cz.adamec.timotej.snag.sync.fe.app.impl.internal.GetSyncStatusUseCaseImpl
import cz.adamec.timotej.snag.sync.fe.app.impl.internal.PullSyncEngine
import cz.adamec.timotej.snag.sync.fe.app.impl.internal.PushSyncEngine
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

    private fun createPushEngine(handlers: List<PushSyncOperationHandler> = emptyList()) =
        PushSyncEngine(
            syncQueue = fakeSyncQueue,
            handlers = handlers,
            applicationScope = applicationScope,
        )

    private fun createPullEngine(handlers: List<PullSyncOperationHandler> = emptyList()) =
        PullSyncEngine(
            handlers = handlers,
            syncCoordinator = createPushEngine(),
        )

    private fun createUseCase(
        pushEngine: PushSyncEngine = createPushEngine(),
        pullEngine: PullSyncEngine = createPullEngine(),
    ) = GetSyncStatusUseCaseImpl(
        pushSyncEngine = pushEngine,
        connectionStatusProvider = fakeConnectionStatusProvider,
        pullSyncEngine = pullEngine,
    )

    @Test
    fun `connected and Idle yields Synced`() =
        runTest(testDispatcher) {
            val useCase = createUseCase()
            fakeConnectionStatusProvider.emit(true)

            useCase().test {
                assertEquals(SyncStatus.Synced, awaitItem())
            }
        }

    @Test
    fun `connected and Pushing yields Syncing`() =
        runTest(testDispatcher) {
            val deferred = CompletableDeferred<PushSyncOperationResult>()
            val handler = SuspendingPushHandler("project", deferred)
            val pushEngine = createPushEngine(listOf(handler))
            val useCase = createUseCase(pushEngine = pushEngine)
            fakeConnectionStatusProvider.emit(true)

            pushEngine.invoke("project", Uuid.random(), SyncOperationType.UPSERT)
            advanceUntilIdle()

            useCase().test {
                assertEquals(SyncStatus.Syncing, awaitItem())
            }

            deferred.complete(PushSyncOperationResult.Success)
            advanceUntilIdle()
        }

    @Test
    fun `connected and push Failed yields Error`() =
        runTest(testDispatcher) {
            val handler = FixedResultPushHandler("project", PushSyncOperationResult.Failure)
            val pushEngine = createPushEngine(listOf(handler))
            val useCase = createUseCase(pushEngine = pushEngine)
            fakeConnectionStatusProvider.emit(true)

            pushEngine.invoke("project", Uuid.random(), SyncOperationType.UPSERT)
            advanceUntilIdle()

            useCase().test {
                assertEquals(SyncStatus.Error, awaitItem())
            }
        }

    @Test
    fun `disconnected and Idle yields Offline`() =
        runTest(testDispatcher) {
            val useCase = createUseCase()
            fakeConnectionStatusProvider.emit(false)

            useCase().test {
                assertEquals(SyncStatus.Offline, awaitItem())
            }
        }

    @Test
    fun `disconnected and Pushing yields Offline`() =
        runTest(testDispatcher) {
            val deferred = CompletableDeferred<PushSyncOperationResult>()
            val handler = SuspendingPushHandler("project", deferred)
            val pushEngine = createPushEngine(listOf(handler))
            val useCase = createUseCase(pushEngine = pushEngine)
            fakeConnectionStatusProvider.emit(false)

            pushEngine.invoke("project", Uuid.random(), SyncOperationType.UPSERT)
            advanceUntilIdle()

            useCase().test {
                assertEquals(SyncStatus.Offline, awaitItem())
            }

            deferred.complete(PushSyncOperationResult.Success)
            advanceUntilIdle()
        }

    @Test
    fun `disconnected and Failed yields Offline`() =
        runTest(testDispatcher) {
            val handler = FixedResultPushHandler("project", PushSyncOperationResult.Failure)
            val pushEngine = createPushEngine(listOf(handler))
            val useCase = createUseCase(pushEngine = pushEngine)
            fakeConnectionStatusProvider.emit(false)

            pushEngine.invoke("project", Uuid.random(), SyncOperationType.UPSERT)
            advanceUntilIdle()

            useCase().test {
                assertEquals(SyncStatus.Offline, awaitItem())
            }
        }

    @Test
    fun `connected and pulling yields Syncing`() =
        runTest(testDispatcher) {
            val deferred = CompletableDeferred<PullSyncOperationResult>()
            val handler = SuspendingPullHandler("project", deferred)
            val pushEngine = createPushEngine()
            val pullEngine =
                PullSyncEngine(
                    handlers = listOf(handler),
                    syncCoordinator = pushEngine,
                )
            val useCase = createUseCase(pushEngine = pushEngine, pullEngine = pullEngine)
            fakeConnectionStatusProvider.emit(true)

            val job =
                launch {
                    pullEngine.invoke("project")
                }
            advanceUntilIdle()

            useCase().test {
                assertEquals(SyncStatus.Syncing, awaitItem())
            }

            deferred.complete(PullSyncOperationResult.Success)
            advanceUntilIdle()
            job.join()
        }

    @Test
    fun `disconnected and pulling yields Offline`() =
        runTest(testDispatcher) {
            val deferred = CompletableDeferred<PullSyncOperationResult>()
            val handler = SuspendingPullHandler("project", deferred)
            val pushEngine = createPushEngine()
            val pullEngine =
                PullSyncEngine(
                    handlers = listOf(handler),
                    syncCoordinator = pushEngine,
                )
            val useCase = createUseCase(pushEngine = pushEngine, pullEngine = pullEngine)
            fakeConnectionStatusProvider.emit(false)

            val job =
                launch {
                    pullEngine.invoke("project")
                }
            advanceUntilIdle()

            useCase().test {
                assertEquals(SyncStatus.Offline, awaitItem())
            }

            deferred.complete(PullSyncOperationResult.Success)
            advanceUntilIdle()
            job.join()
        }

    @Test
    fun `connected and pull Failed yields Error`() =
        runTest(testDispatcher) {
            val handler = FixedResultPullHandler("project", PullSyncOperationResult.Failure)
            val pushEngine = createPushEngine()
            val pullEngine =
                PullSyncEngine(
                    handlers = listOf(handler),
                    syncCoordinator = pushEngine,
                )
            val useCase = createUseCase(pushEngine = pushEngine, pullEngine = pullEngine)
            fakeConnectionStatusProvider.emit(true)

            pullEngine.invoke("project")

            useCase().test {
                assertEquals(SyncStatus.Error, awaitItem())
            }
        }

    private class FakeConnectionStatusProvider : ConnectionStatusProvider {
        private val connectedFlow = MutableStateFlow(true)

        fun emit(connected: Boolean) {
            connectedFlow.value = connected
        }

        override fun isConnectedFlow(): Flow<Boolean> = connectedFlow
    }

    private class FixedResultPushHandler(
        override val entityTypeId: String,
        private val result: PushSyncOperationResult,
    ) : PushSyncOperationHandler {
        override suspend fun execute(
            entityId: Uuid,
            operationType: SyncOperationType,
        ): PushSyncOperationResult = result
    }

    private class SuspendingPushHandler(
        override val entityTypeId: String,
        private val deferred: CompletableDeferred<PushSyncOperationResult>,
    ) : PushSyncOperationHandler {
        override suspend fun execute(
            entityId: Uuid,
            operationType: SyncOperationType,
        ): PushSyncOperationResult = deferred.await()
    }

    private class FixedResultPullHandler(
        override val entityTypeId: String,
        private val result: PullSyncOperationResult,
    ) : PullSyncOperationHandler {
        override suspend fun execute(scopeId: Uuid?): PullSyncOperationResult = result
    }

    private class SuspendingPullHandler(
        override val entityTypeId: String,
        private val deferred: CompletableDeferred<PullSyncOperationResult>,
    ) : PullSyncOperationHandler {
        override suspend fun execute(scopeId: Uuid?): PullSyncOperationResult = deferred.await()
    }
}
