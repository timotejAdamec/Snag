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

package cz.adamec.timotej.snag.lib.sync.fe.app.api.handler

import co.touchlab.kermit.Logger
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.common.TimestampProvider
import cz.adamec.timotej.snag.lib.core.common.UuidProvider
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.lib.sync.fe.model.SyncOperationType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.uuid.Uuid

class DbApiSyncHandlerTest {
    private val testDispatcher = StandardTestDispatcher()

    private data class TestEntity(
        val id: Uuid,
        val name: String,
    )

    private class TestDbApiSyncHandler(
        private val db: MutableMap<Uuid, TestEntity> = mutableMapOf(),
        var apiSaveResponse: ((TestEntity) -> OnlineDataResult<TestEntity?>)? = null,
        var apiDeleteResponse: ((Uuid) -> OnlineDataResult<TestEntity?>)? = null,
        var apiForcedFailure: OnlineDataResult.Failure? = null,
    ) : DbApiSyncHandler<TestEntity>(
            logger = Logger.withTag("TestDbApiSyncHandler"),
            timestampProvider =
                object : TimestampProvider {
                    override fun getNowTimestamp(): Timestamp = Timestamp(0L)
                },
        ) {
        override val entityTypeId: String = "test-entity"
        override val entityName: String = "test entity"

        var lastSavedToApi: TestEntity? = null
        var lastDeletedFromApiId: Uuid? = null

        override fun getEntityFlow(entityId: Uuid): Flow<OfflineFirstDataResult<TestEntity?>> =
            flowOf(OfflineFirstDataResult.Success(db[entityId]))

        override suspend fun saveEntityToApi(entity: TestEntity): OnlineDataResult<TestEntity?> {
            lastSavedToApi = entity
            apiForcedFailure?.let { return it }
            return apiSaveResponse?.invoke(entity)
                ?: OnlineDataResult.Success(entity)
        }

        override suspend fun deleteEntityFromApi(
            entityId: Uuid,
            deletedAt: Timestamp,
        ): OnlineDataResult<TestEntity?> {
            lastDeletedFromApiId = entityId
            apiForcedFailure?.let { return it }
            return apiDeleteResponse?.invoke(entityId)
                ?: OnlineDataResult.Success(null)
        }

        override suspend fun saveEntityToDb(entity: TestEntity): OfflineFirstDataResult<Unit> {
            db[entity.id] = entity
            return OfflineFirstDataResult.Success(Unit)
        }

        fun getDbEntity(id: Uuid): TestEntity? = db[id]
    }

    private fun createEntity(name: String = "Test") =
        TestEntity(
            id = UuidProvider.getUuid(),
            name = name,
        )

    @Test
    fun `executeUpsert reads from db and calls api`() =
        runTest(testDispatcher) {
            val entity = createEntity()
            val handler = TestDbApiSyncHandler(db = mutableMapOf(entity.id to entity))

            val result = handler.execute(entity.id, SyncOperationType.UPSERT)

            assertEquals(SyncOperationResult.Success, result)
            assertEquals(entity, handler.lastSavedToApi)
        }

    @Test
    fun `executeUpsert saves fresher entity from api to db`() =
        runTest(testDispatcher) {
            val entity = createEntity(name = "Original")
            val fresherEntity = entity.copy(name = "Updated by API")
            val db = mutableMapOf(entity.id to entity)
            val handler =
                TestDbApiSyncHandler(
                    db = db,
                    apiSaveResponse = { OnlineDataResult.Success(fresherEntity) },
                )

            val result = handler.execute(entity.id, SyncOperationType.UPSERT)

            assertEquals(SyncOperationResult.Success, result)
            assertEquals("Updated by API", handler.getDbEntity(entity.id)?.name)
        }

    @Test
    fun `executeUpsert when entity not in db returns entity not found`() =
        runTest(testDispatcher) {
            val handler = TestDbApiSyncHandler()

            val result = handler.execute(UuidProvider.getUuid(), SyncOperationType.UPSERT)

            assertEquals(SyncOperationResult.EntityNotFound, result)
        }

    @Test
    fun `executeUpsert when api fails returns failure`() =
        runTest(testDispatcher) {
            val entity = createEntity()
            val db = mutableMapOf(entity.id to entity)
            val handler =
                TestDbApiSyncHandler(
                    db = db,
                    apiForcedFailure = OnlineDataResult.Failure.ProgrammerError(Exception("API error")),
                )

            val result = handler.execute(entity.id, SyncOperationType.UPSERT)

            assertEquals(SyncOperationResult.Failure, result)
            assertEquals(entity, handler.getDbEntity(entity.id))
        }

    @Test
    fun `executeDelete calls api and returns success`() =
        runTest(testDispatcher) {
            val entityId = UuidProvider.getUuid()
            val handler = TestDbApiSyncHandler()

            val result = handler.execute(entityId, SyncOperationType.DELETE)

            assertEquals(SyncOperationResult.Success, result)
            assertEquals(entityId, handler.lastDeletedFromApiId)
        }

    @Test
    fun `executeDelete saves entity returned by api to db`() =
        runTest(testDispatcher) {
            val entity = createEntity(name = "Restored by API")
            val db = mutableMapOf<Uuid, TestEntity>()
            val handler =
                TestDbApiSyncHandler(
                    db = db,
                    apiDeleteResponse = { OnlineDataResult.Success(entity) },
                )

            val result = handler.execute(entity.id, SyncOperationType.DELETE)

            assertEquals(SyncOperationResult.Success, result)
            assertEquals(entity, handler.getDbEntity(entity.id))
        }

    @Test
    fun `executeDelete when api returns null does not save to db`() =
        runTest(testDispatcher) {
            val entityId = UuidProvider.getUuid()
            val db = mutableMapOf<Uuid, TestEntity>()
            val handler = TestDbApiSyncHandler(db = db)

            val result = handler.execute(entityId, SyncOperationType.DELETE)

            assertEquals(SyncOperationResult.Success, result)
            assertNull(handler.getDbEntity(entityId))
        }

    @Test
    fun `executeDelete when api fails returns failure`() =
        runTest(testDispatcher) {
            val handler =
                TestDbApiSyncHandler(
                    apiForcedFailure = OnlineDataResult.Failure.ProgrammerError(Exception("API error")),
                )

            val result = handler.execute(UuidProvider.getUuid(), SyncOperationType.DELETE)

            assertEquals(SyncOperationResult.Failure, result)
        }
}
