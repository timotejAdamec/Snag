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
import cz.adamec.timotej.snag.sync.fe.app.api.handler.PullSyncOperationHandler
import cz.adamec.timotej.snag.sync.fe.app.api.handler.PullSyncOperationResult
import cz.adamec.timotej.snag.sync.fe.app.impl.internal.PullSyncEngine
import cz.adamec.timotej.snag.sync.fe.app.impl.internal.PullSyncEngineStatus
import cz.adamec.timotej.snag.sync.fe.app.impl.internal.PushSyncEngine
import cz.adamec.timotej.snag.sync.fe.driven.test.FakeSyncQueue
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(ExperimentalCoroutinesApi::class)
class PullSyncEngineTest : FrontendKoinInitializedTest() {
    private val fakeSyncQueue: FakeSyncQueue by inject()
    private val applicationScope: ApplicationScope by inject()

    private fun createSyncCoordinator() =
        PushSyncEngine(
            syncQueue = fakeSyncQueue,
            handlers = emptyList(),
            applicationScope = applicationScope,
        )

    private fun createEngine(handlers: List<PullSyncOperationHandler> = emptyList()) =
        PullSyncEngine(
            handlers = handlers,
            syncCoordinator = createSyncCoordinator(),
        )

    @Test
    fun `status is Idle when no pulls active`() =
        runTest(testDispatcher) {
            val engine = createEngine()

            engine.status.test {
                assertEquals(PullSyncEngineStatus.Idle, awaitItem())
            }
        }

    @Test
    fun `status transitions Idle to Pulling to Idle on success`() =
        runTest(testDispatcher) {
            val deferred = CompletableDeferred<PullSyncOperationResult>()
            val handler = SuspendingHandler("project", deferred)
            val engine = createEngine(listOf(handler))

            engine.status.test {
                assertEquals(PullSyncEngineStatus.Idle, awaitItem())

                val job = launch { engine.invoke("project") }
                advanceUntilIdle()
                assertEquals(PullSyncEngineStatus.Pulling, awaitItem())

                deferred.complete(PullSyncOperationResult.Success)
                advanceUntilIdle()
                assertEquals(PullSyncEngineStatus.Idle, awaitItem())
                job.join()
            }
        }

    @Test
    fun `status transitions Idle to Pulling to Failed on failure`() =
        runTest(testDispatcher) {
            val deferred = CompletableDeferred<PullSyncOperationResult>()
            val handler = SuspendingHandler("project", deferred)
            val engine = createEngine(listOf(handler))

            engine.status.test {
                assertEquals(PullSyncEngineStatus.Idle, awaitItem())

                val job = launch { engine.invoke("project") }
                advanceUntilIdle()
                assertEquals(PullSyncEngineStatus.Pulling, awaitItem())

                deferred.complete(PullSyncOperationResult.Failure)
                advanceUntilIdle()
                assertEquals(PullSyncEngineStatus.Failed, awaitItem())
                job.join()
            }
        }

    @Test
    fun `concurrent pulls - stays Pulling until all complete`() =
        runTest(testDispatcher) {
            val deferred1 = CompletableDeferred<PullSyncOperationResult>()
            val deferred2 = CompletableDeferred<PullSyncOperationResult>()
            val handler1 = SuspendingHandler("project", deferred1)
            val handler2 = SuspendingHandler("client", deferred2)
            val engine = createEngine(listOf(handler1, handler2))

            engine.status.test {
                assertEquals(PullSyncEngineStatus.Idle, awaitItem())

                val job1 = launch { engine.invoke("project") }
                advanceUntilIdle()
                assertEquals(PullSyncEngineStatus.Pulling, awaitItem())

                val job2 = launch { engine.invoke("client") }
                advanceUntilIdle()

                deferred1.complete(PullSyncOperationResult.Success)
                advanceUntilIdle()
                // Still pulling because job2 is active
                expectNoEvents()

                deferred2.complete(PullSyncOperationResult.Success)
                advanceUntilIdle()
                assertEquals(PullSyncEngineStatus.Idle, awaitItem())
                job1.join()
                job2.join()
            }
        }

    @Test
    fun `concurrent pulls - Failed if any fails`() =
        runTest(testDispatcher) {
            val deferred1 = CompletableDeferred<PullSyncOperationResult>()
            val deferred2 = CompletableDeferred<PullSyncOperationResult>()
            val handler1 = SuspendingHandler("project", deferred1)
            val handler2 = SuspendingHandler("client", deferred2)
            val engine = createEngine(listOf(handler1, handler2))

            engine.status.test {
                assertEquals(PullSyncEngineStatus.Idle, awaitItem())

                val job1 = launch { engine.invoke("project") }
                advanceUntilIdle()
                assertEquals(PullSyncEngineStatus.Pulling, awaitItem())

                val job2 = launch { engine.invoke("client") }
                advanceUntilIdle()

                deferred1.complete(PullSyncOperationResult.Failure)
                advanceUntilIdle()

                deferred2.complete(PullSyncOperationResult.Success)
                advanceUntilIdle()
                assertEquals(PullSyncEngineStatus.Failed, awaitItem())
                job1.join()
                job2.join()
            }
        }

    @Test
    fun `retry clears failure`() =
        runTest(testDispatcher) {
            val handler = FixedResultHandler("project", PullSyncOperationResult.Failure)
            val engine = createEngine(listOf(handler))

            engine.invoke("project")
            assertEquals(PullSyncEngineStatus.Failed, engine.status.value)

            handler.result = PullSyncOperationResult.Success
            engine.invoke("project")
            assertEquals(PullSyncEngineStatus.Idle, engine.status.value)
        }

    @Test
    fun `missing handler throws`() =
        runTest(testDispatcher) {
            val engine = createEngine(emptyList())

            assertFailsWith<IllegalStateException> {
                engine.invoke("nonexistent")
            }
        }

    private class SuspendingHandler(
        override val entityTypeId: String,
        private val deferred: CompletableDeferred<PullSyncOperationResult>,
    ) : PullSyncOperationHandler {
        override suspend fun execute(scopeId: String): PullSyncOperationResult = deferred.await()
    }

    private class FixedResultHandler(
        override val entityTypeId: String,
        var result: PullSyncOperationResult,
    ) : PullSyncOperationHandler {
        override suspend fun execute(scopeId: String): PullSyncOperationResult = result
    }
}
