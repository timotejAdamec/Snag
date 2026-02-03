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

import cz.adamec.timotej.snag.lib.core.common.ApplicationScope
import cz.adamec.timotej.snag.lib.sync.business.SyncOperationType
import cz.adamec.timotej.snag.lib.sync.fe.app.api.handler.SyncOperationHandler
import cz.adamec.timotej.snag.lib.sync.fe.app.api.handler.SyncOperationResult
import cz.adamec.timotej.snag.lib.sync.fe.app.impl.internal.SyncEngine
import cz.adamec.timotej.snag.lib.sync.fe.driven.test.FakeSyncQueue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class SyncEngineTest {
    private val testDispatcher = StandardTestDispatcher()
    private val applicationScope =
        object : ApplicationScope, CoroutineScope by CoroutineScope(testDispatcher) {}

    private lateinit var syncQueue: FakeSyncQueue

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        syncQueue = FakeSyncQueue()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createEngine(handlers: List<SyncOperationHandler> = emptyList()) =
        SyncEngine(
            syncQueue = syncQueue,
            handlers = handlers,
            applicationScope = applicationScope,
        )

    @Test
    fun `successful operation is removed from queue`() =
        runTest {
            val handler = TestSyncHandler("project", SyncOperationResult.Success)
            val engine = createEngine(listOf(handler))

            engine.invoke("project", Uuid.random(), SyncOperationType.UPSERT)
            advanceUntilIdle()

            assertTrue(syncQueue.getAllPending().isEmpty())
            assertEquals(1, handler.executedOperations.size)
        }

    @Test
    fun `failed operation stays in queue and stops processing`() =
        runTest {
            val handler = TestSyncHandler("project", SyncOperationResult.Failure)
            val engine = createEngine(listOf(handler))
            val id1 = Uuid.random()
            val id2 = Uuid.random()

            syncQueue.enqueue("project", id1, SyncOperationType.UPSERT)
            syncQueue.enqueue("project", id2, SyncOperationType.UPSERT)
            engine.invoke("project", Uuid.random(), SyncOperationType.UPSERT)
            advanceUntilIdle()

            // First op failed, processing stopped — all 3 remain
            assertEquals(3, syncQueue.getAllPending().size)
            // Only first operation was attempted
            assertEquals(1, handler.executedOperations.size)
        }

    @Test
    fun `entity not found discards operation and continues`() =
        runTest {
            val handler = TestSyncHandler("project", SyncOperationResult.EntityNotFound)
            val engine = createEngine(listOf(handler))

            engine.invoke("project", Uuid.random(), SyncOperationType.UPSERT)
            engine.invoke("project", Uuid.random(), SyncOperationType.UPSERT)
            advanceUntilIdle()

            assertTrue(syncQueue.getAllPending().isEmpty())
            assertEquals(2, handler.executedOperations.size)
        }

    @Test
    fun `missing handler throws`() =
        runTest {
            val engine =
                SyncEngine(
                    syncQueue = syncQueue,
                    handlers = emptyList(),
                    applicationScope = applicationScope,
                )

            assertFailsWith<IllegalArgumentException> {
                engine.invoke("project", Uuid.random(), SyncOperationType.UPSERT)
            }
        }

    @Test
    fun `deduplication replaces operation type`() =
        runTest {
            val handler = TestSyncHandler("project", SyncOperationResult.Success)
            val engine = createEngine(listOf(handler))
            val entityId = Uuid.random()

            engine.invoke("project", entityId, SyncOperationType.UPSERT)
            engine.invoke("project", entityId, SyncOperationType.DELETE)
            advanceUntilIdle()

            assertTrue(syncQueue.getAllPending().isEmpty())
            assertEquals(1, handler.executedOperations.size)
            assertEquals(SyncOperationType.DELETE, handler.executedOperations[0].second)
        }

    @Test
    fun `new enqueue triggers retry of previously failed ops`() =
        runTest {
            var shouldFail = true
            val handler =
                object : SyncOperationHandler {
                    override val entityTypeId = "project"
                    val executedOperations = mutableListOf<Pair<Uuid, SyncOperationType>>()

                    override suspend fun execute(
                        entityId: Uuid,
                        operationType: SyncOperationType,
                    ): SyncOperationResult {
                        executedOperations.add(entityId to operationType)
                        return if (shouldFail) SyncOperationResult.Failure else SyncOperationResult.Success
                    }
                }
            val engine = createEngine(listOf(handler))
            val id1 = Uuid.random()

            engine.invoke("project", id1, SyncOperationType.UPSERT)
            advanceUntilIdle()
            assertEquals(1, syncQueue.getAllPending().size)

            shouldFail = false
            val id2 = Uuid.random()
            engine.invoke("project", id2, SyncOperationType.UPSERT)
            advanceUntilIdle()

            assertTrue(syncQueue.getAllPending().isEmpty())
            assertEquals(3, handler.executedOperations.size)
        }

    private class TestSyncHandler(
        override val entityTypeId: String,
        private val result: SyncOperationResult,
    ) : SyncOperationHandler {
        val executedOperations = mutableListOf<Pair<Uuid, SyncOperationType>>()

        override suspend fun execute(
            entityId: Uuid,
            operationType: SyncOperationType,
        ): SyncOperationResult {
            executedOperations.add(entityId to operationType)
            return result
        }
    }
}
