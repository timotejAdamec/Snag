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
import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsDb
import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsPullSyncCoordinator
import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsPullSyncTimestampDataSource
import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsSync
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsDb
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsPullSyncCoordinator
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsPullSyncTimestampDataSource
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsSync
import cz.adamec.timotej.snag.lib.core.common.ApplicationScope
import cz.adamec.timotej.snag.lib.sync.fe.app.api.handler.SyncOperationHandler
import cz.adamec.timotej.snag.lib.sync.fe.app.api.handler.SyncOperationResult
import cz.adamec.timotej.snag.lib.sync.fe.app.impl.internal.GetSyncStatusUseCaseImpl
import cz.adamec.timotej.snag.lib.sync.fe.app.impl.internal.SyncEngine
import cz.adamec.timotej.snag.lib.sync.fe.driven.test.FakeSyncQueue
import cz.adamec.timotej.snag.lib.sync.fe.model.SyncOperationType
import cz.adamec.timotej.snag.lib.sync.fe.model.SyncStatus
import cz.adamec.timotej.snag.lib.sync.fe.ports.SyncQueue
import cz.adamec.timotej.snag.network.fe.test.FakeInternetConnectionStatusListener
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import kotlinx.coroutines.CompletableDeferred
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
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class GetSyncStatusUseCaseImplTest : FrontendKoinInitializedTest() {
    private val fakeSyncQueue: FakeSyncQueue by inject()
    private val applicationScope: ApplicationScope by inject()
    private val fakeConnectionListener = FakeInternetConnectionStatusListener()

    override fun additionalKoinModules(): List<Module> =
        listOf(
            module {
                singleOf(::FakeSyncQueue) bind SyncQueue::class
                singleOf(::FakeInspectionsDb) bind InspectionsDb::class
                singleOf(::FakeInspectionsSync) bind InspectionsSync::class
                singleOf(::FakeInspectionsPullSyncCoordinator) bind InspectionsPullSyncCoordinator::class
                singleOf(::FakeInspectionsPullSyncTimestampDataSource) bind InspectionsPullSyncTimestampDataSource::class
            },
        )

    private fun createEngine(handlers: List<SyncOperationHandler> = emptyList()) =
        SyncEngine(
            syncQueue = fakeSyncQueue,
            handlers = handlers,
            applicationScope = applicationScope,
        )

    private fun createUseCase(engine: SyncEngine) =
        GetSyncStatusUseCaseImpl(
            syncEngine = engine,
            connectionStatusListener = fakeConnectionListener,
        )

    @Test
    fun `connected and Idle yields Synced`() =
        runTest(testDispatcher) {
            val engine = createEngine()
            val useCase = createUseCase(engine)
            fakeConnectionListener.emit(true)

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
            fakeConnectionListener.emit(true)

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
            fakeConnectionListener.emit(true)

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
            fakeConnectionListener.emit(false)

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
            fakeConnectionListener.emit(false)

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
            fakeConnectionListener.emit(false)

            engine.invoke("project", Uuid.random(), SyncOperationType.UPSERT)
            advanceUntilIdle()

            useCase().test {
                assertEquals(SyncStatus.Offline, awaitItem())
            }
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
