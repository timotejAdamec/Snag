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
import cz.adamec.timotej.snag.lib.sync.fe.model.SyncOperationType
import cz.adamec.timotej.snag.lib.sync.fe.app.api.handler.SyncOperationHandler
import cz.adamec.timotej.snag.lib.sync.fe.app.api.handler.SyncOperationResult
import cz.adamec.timotej.snag.lib.sync.fe.app.impl.internal.SyncEngine
import cz.adamec.timotej.snag.lib.sync.fe.driven.test.FakeSyncQueue
import cz.adamec.timotej.snag.lib.sync.fe.ports.SyncQueue
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class SyncEngineTest : FrontendKoinInitializedTest() {

    private val fakeSyncQueue: FakeSyncQueue by inject()
    private val applicationScope: ApplicationScope by inject()

    override fun additionalKoinModules(): List<Module> =
        listOf(
            module {
                singleOf(::FakeSyncQueue) bind SyncQueue::class
            },
        )

    private fun createEngine(handlers: List<SyncOperationHandler> = emptyList()) =
        SyncEngine(
            syncQueue = fakeSyncQueue,
            handlers = handlers,
            applicationScope = applicationScope,
        )

    @Test
    fun `successful operation is removed from queue`() =
        runTest(testDispatcher) {
            val handler = TestSyncHandler("project", SyncOperationResult.Success)
            val engine = createEngine(listOf(handler))

            engine.invoke("project", Uuid.random(), SyncOperationType.UPSERT)
            advanceUntilIdle()

            assertTrue(fakeSyncQueue.getAllPending().isEmpty())
            assertEquals(1, handler.executedOperations.size)
        }

    @Test
    fun `failed operation stays in queue and stops processing`() =
        runTest(testDispatcher) {
            val handler = TestSyncHandler("project", SyncOperationResult.Failure)
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
            val handler = TestSyncHandler("project", SyncOperationResult.EntityNotFound)
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
                SyncEngine(
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
            val handler = TestSyncHandler("project", SyncOperationResult.Success)
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
            assertEquals(1, fakeSyncQueue.getAllPending().size)

            shouldFail = false
            val id2 = Uuid.random()
            engine.invoke("project", id2, SyncOperationType.UPSERT)
            advanceUntilIdle()

            assertTrue(fakeSyncQueue.getAllPending().isEmpty())
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
