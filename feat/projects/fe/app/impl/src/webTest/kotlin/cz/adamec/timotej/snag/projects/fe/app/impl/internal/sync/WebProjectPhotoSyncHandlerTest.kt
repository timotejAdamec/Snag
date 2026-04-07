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

package cz.adamec.timotej.snag.projects.fe.app.impl.internal.sync

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.projects.app.model.AppProjectPhotoData
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectPhotosApi
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectPhotosDb
import cz.adamec.timotej.snag.sync.fe.app.api.handler.PushSyncOperationHandler
import cz.adamec.timotej.snag.sync.fe.app.api.handler.PushSyncOperationResult
import cz.adamec.timotej.snag.sync.fe.model.SyncOperationType
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import kotlinx.coroutines.test.runTest
import org.koin.mp.KoinPlatform.getKoin
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

class WebProjectPhotoSyncHandlerTest : FrontendKoinInitializedTest() {
    private val fakeProjectPhotosDb: FakeProjectPhotosDb by inject()
    private val fakeProjectPhotosApi: FakeProjectPhotosApi by inject()

    private val handler: PushSyncOperationHandler by lazy {
        getKoin()
            .getAll<PushSyncOperationHandler>()
            .first { it.entityTypeId == PROJECT_PHOTO_SYNC_ENTITY_TYPE }
    }

    private val projectId = Uuid.parse("00000000-0000-0000-0000-000000000001")
    private val photoId = Uuid.parse("00000000-0000-0000-0002-000000000001")

    private fun createPhoto(id: Uuid = photoId) =
        AppProjectPhotoData(
            id = id,
            projectId = projectId,
            url = "https://example.com/photo.jpg",
            description = "Test photo",
            updatedAt = Timestamp(10L),
        )

    @Test
    fun `upsert saves photo to API`() =
        runTest(testDispatcher) {
            fakeProjectPhotosDb.setPhoto(createPhoto())

            val result =
                handler.execute(
                    entityId = photoId,
                    operationType = SyncOperationType.UPSERT,
                )

            assertEquals(PushSyncOperationResult.Success, result)
        }

    @Test
    fun `upsert returns entity not found when photo missing from DB`() =
        runTest(testDispatcher) {
            val result =
                handler.execute(
                    entityId = photoId,
                    operationType = SyncOperationType.UPSERT,
                )

            assertEquals(PushSyncOperationResult.EntityNotFound, result)
        }

    @Test
    fun `upsert returns failure when API fails`() =
        runTest(testDispatcher) {
            fakeProjectPhotosDb.setPhoto(createPhoto())
            fakeProjectPhotosApi.forcedFailure = OnlineDataResult.Failure.NetworkUnavailable

            val result =
                handler.execute(
                    entityId = photoId,
                    operationType = SyncOperationType.UPSERT,
                )

            assertEquals(PushSyncOperationResult.Failure, result)
        }

    @Test
    fun `delete calls API`() =
        runTest(testDispatcher) {
            val result =
                handler.execute(
                    entityId = photoId,
                    operationType = SyncOperationType.DELETE,
                    scopeId = projectId,
                )

            assertEquals(PushSyncOperationResult.Success, result)
        }

    @Test
    fun `delete returns failure when API fails`() =
        runTest(testDispatcher) {
            fakeProjectPhotosApi.forcedFailure =
                OnlineDataResult.Failure.ProgrammerError(RuntimeException("API error"))

            val result =
                handler.execute(
                    entityId = photoId,
                    operationType = SyncOperationType.DELETE,
                    scopeId = projectId,
                )

            assertEquals(PushSyncOperationResult.Failure, result)
        }
}
