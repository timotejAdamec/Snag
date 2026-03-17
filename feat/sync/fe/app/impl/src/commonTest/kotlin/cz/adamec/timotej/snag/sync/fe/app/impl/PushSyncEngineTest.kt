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
import cz.adamec.timotej.snag.sync.fe.app.api.handler.PushSyncOperationHandler
import cz.adamec.timotej.snag.sync.fe.app.api.handler.PushSyncOperationResult
import cz.adamec.timotej.snag.sync.fe.app.impl.internal.PushSyncEngine
import cz.adamec.timotej.snag.sync.fe.app.impl.internal.PushSyncEngineStatus
import cz.adamec.timotej.snag.sync.fe.driven.test.FakeSyncQueue
import cz.adamec.timotej.snag.sync.fe.model.SyncOperationType
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class PushSyncEngineTest : FrontendKoinInitializedTest() {
    private val fakeSyncQueue: FakeSyncQueue by inject()
    private val applicationScope: ApplicationScope by inject()

    private fun createEngine(handlers: List<PushSyncOperationHandler> = emptyList()) =
        PushSyncEngine(
            syncQueue = fakeSyncQueue,
            handlers = handlers,
            applicationScope = applicationScope,
        )

    @Test
    fun `successful operation is removed from queue`() =
        runTest(testDispatcher) {
            val handler = TestSyncHandler("project", PushSyncOperationResult.Success)
            val engine = createEngine(listOf(handler))

            engine.invoke("project", Uuid.random(), SyncOperationType.UPSERT)
            advanceUntilIdle()

            assertTrue(fakeSyncQueue.getAllPending().isEmpty())
            assertEquals(1, handler.executedOperations.size)
        }

    @Test
    fun `failed operation stays in queue and stops processing`() =
        runTest(testDispatcher) {
            val handler = TestSyncHandler("project", PushSyncOperationResult.Failure)
            val engine = createEngine(listOf(handler))
            val id1 = Uuid.random()
            val id2 = Uuid.random()

            fakeSyncQueue.enqueue("project", id1, SyncOperationType.UPSERT)
            fakeSyncQueue.enqueue("project", id2, SyncOperationType.UPSERT)
            engine.invoke("project", Uuid.random(), SyncOperationType.UPSERT)
            advanceUntilIdle()

            // First op failed, processing stopped — all 3 remain
            assertEquals(3, fakeSyncQueue.getAllPending().size)
            // Only first operation was attempted
            assertEquals(1, handler.executedOperations.size)
        }

    @Test
    fun `entity not found discards operation and continues`() =
        runTest(testDispatcher) {
            val handler = TestSyncHandler("project", PushSyncOperationResult.EntityNotFound)
            val engine = createEngine(listOf(handler))

            engine.invoke("project", Uuid.random(), SyncOperationType.UPSERT)
            engine.invoke("project", Uuid.random(), SyncOperationType.UPSERT)
            advanceUntilIdle()

            assertTrue(fakeSyncQueue.getAllPending().isEmpty())
            assertEquals(2, handler.executedOperations.size)
        }

    @Test
    fun `missing handler throws`() =
        runTest(testDispatcher) {
            val engine =
                PushSyncEngine(
                    syncQueue = fakeSyncQueue,
                    handlers = emptyList(),
                    applicationScope = applicationScope,
                )

            assertFailsWith<IllegalArgumentException> {
                engine.invoke("project", Uuid.random(), SyncOperationType.UPSERT)
            }
        }

    @Test
    fun `deduplication replaces operation type`() =
        runTest(testDispatcher) {
            val handler = TestSyncHandler("project", PushSyncOperationResult.Success)
            val engine = createEngine(listOf(handler))
            val entityId = Uuid.random()

            engine.invoke("project", entityId, SyncOperationType.UPSERT)
            engine.invoke("project", entityId, SyncOperationType.DELETE)
            advanceUntilIdle()

            assertTrue(fakeSyncQueue.getAllPending().isEmpty())
            assertEquals(1, handler.executedOperations.size)
            assertEquals(SyncOperationType.DELETE, handler.executedOperations[0].second)
        }

    @Test
    fun `new enqueue triggers retry of previously failed ops`() =
        runTest(testDispatcher) {
            var shouldFail = true
            val handler =
                object : PushSyncOperationHandler {
                    override val entityTypeId = "project"
                    val executedOperations = mutableListOf<Pair<Uuid, SyncOperationType>>()

                    override suspend fun execute(
                        entityId: Uuid,
                        operationType: SyncOperationType,
                    ): PushSyncOperationResult {
                        executedOperations.add(entityId to operationType)
                        return if (shouldFail) PushSyncOperationResult.Failure else PushSyncOperationResult.Success
                    }
                }
            val engine = createEngine(listOf(handler))
            val id1 = Uuid.random()

            engine.invoke("project", id1, SyncOperationType.UPSERT)
            advanceUntilIdle()
            assertEquals(1, fakeSyncQueue.getAllPending().size)

            shouldFail = false
            val id2 = Uuid.random()
            engine.invoke("project", id2, SyncOperationType.UPSERT)
            advanceUntilIdle()

            assertTrue(fakeSyncQueue.getAllPending().isEmpty())
            assertEquals(3, handler.executedOperations.size)
        }

    @Test
    fun `status is Idle when queue is empty`() =
        runTest(testDispatcher) {
            val engine = createEngine(emptyList())

            engine.status.test {
                assertEquals(PushSyncEngineStatus.Idle, awaitItem())
            }
        }

    @Test
    fun `status transitions Idle to Pushing to Idle on success`() =
        runTest(testDispatcher) {
            val deferred = CompletableDeferred<PushSyncOperationResult>()
            val handler = SuspendingHandler("project", deferred)
            val engine = createEngine(listOf(handler))

            engine.status.test {
                assertEquals(PushSyncEngineStatus.Idle, awaitItem())

                engine.invoke("project", Uuid.random(), SyncOperationType.UPSERT)
                advanceUntilIdle()
                assertEquals(PushSyncEngineStatus.Pushing, awaitItem())

                deferred.complete(PushSyncOperationResult.Success)
                advanceUntilIdle()
                assertEquals(PushSyncEngineStatus.Idle, awaitItem())
            }
        }

    @Test
    fun `status transitions Idle to Pushing to Failed on failure`() =
        runTest(testDispatcher) {
            val deferred = CompletableDeferred<PushSyncOperationResult>()
            val handler = SuspendingHandler("project", deferred)
            val engine = createEngine(listOf(handler))

            engine.status.test {
                assertEquals(PushSyncEngineStatus.Idle, awaitItem())

                engine.invoke("project", Uuid.random(), SyncOperationType.UPSERT)
                advanceUntilIdle()
                assertEquals(PushSyncEngineStatus.Pushing, awaitItem())

                deferred.complete(PushSyncOperationResult.Failure)
                advanceUntilIdle()
                assertEquals(PushSyncEngineStatus.Failed, awaitItem())
            }
        }

    @Test
    fun `status returns to Idle after retry succeeds`() =
        runTest(testDispatcher) {
            val deferred1 = CompletableDeferred<PushSyncOperationResult>()
            val deferred2 = CompletableDeferred<PushSyncOperationResult>()
            var callCount = 0
            val handler =
                object : PushSyncOperationHandler {
                    override val entityTypeId = "project"

                    override suspend fun execute(
                        entityId: Uuid,
                        operationType: SyncOperationType,
                    ): PushSyncOperationResult =
                        when (++callCount) {
                            1 -> deferred1.await()
                            2 -> deferred2.await()
                            else -> PushSyncOperationResult.Success
                        }
                }
            val engine = createEngine(listOf(handler))

            engine.status.test {
                assertEquals(PushSyncEngineStatus.Idle, awaitItem())

                engine.invoke("project", Uuid.random(), SyncOperationType.UPSERT)
                advanceUntilIdle()
                assertEquals(PushSyncEngineStatus.Pushing, awaitItem())

                deferred1.complete(PushSyncOperationResult.Failure)
                advanceUntilIdle()
                assertEquals(PushSyncEngineStatus.Failed, awaitItem())

                engine.invoke("project", Uuid.random(), SyncOperationType.UPSERT)
                advanceUntilIdle()
                assertEquals(PushSyncEngineStatus.Pushing, awaitItem())

                deferred2.complete(PushSyncOperationResult.Success)
                advanceUntilIdle()
                assertEquals(PushSyncEngineStatus.Idle, awaitItem())
            }
        }

    private class SuspendingHandler(
        override val entityTypeId: String,
        private val deferred: CompletableDeferred<PushSyncOperationResult>,
    ) : PushSyncOperationHandler {
        override suspend fun execute(
            entityId: Uuid,
            operationType: SyncOperationType,
        ): PushSyncOperationResult = deferred.await()
    }

    private class TestSyncHandler(
        override val entityTypeId: String,
        private val result: PushSyncOperationResult,
    ) : PushSyncOperationHandler {
        val executedOperations = mutableListOf<Pair<Uuid, SyncOperationType>>()

        override suspend fun execute(
            entityId: Uuid,
            operationType: SyncOperationType,
        ): PushSyncOperationResult {
            executedOperations.add(entityId to operationType)
            return result
        }
    }
}
