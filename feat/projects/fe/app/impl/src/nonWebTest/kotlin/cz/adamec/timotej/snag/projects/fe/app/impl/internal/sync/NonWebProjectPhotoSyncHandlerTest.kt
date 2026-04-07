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
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.lib.storage.fe.test.FakeFileApi
import cz.adamec.timotej.snag.lib.storage.fe.test.FakeLocalFileStorage
import cz.adamec.timotej.snag.projects.app.model.AppProjectPhotoData
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectPhotosApi
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectPhotosDb
import cz.adamec.timotej.snag.sync.fe.app.api.handler.PushSyncOperationHandler
import cz.adamec.timotej.snag.sync.fe.app.api.handler.PushSyncOperationResult
import cz.adamec.timotej.snag.sync.fe.model.SyncOperationType
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.koin.mp.KoinPlatform.getKoin
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class NonWebProjectPhotoSyncHandlerTest : FrontendKoinInitializedTest() {
    private val fakeProjectPhotosDb: FakeProjectPhotosDb by inject()
    private val fakeProjectPhotosApi: FakeProjectPhotosApi by inject()
    private val fakeFileApi: FakeFileApi by inject()
    private val fakeLocalFileStorage: FakeLocalFileStorage by inject()

    private val handler: PushSyncOperationHandler by lazy {
        getKoin()
            .getAll<PushSyncOperationHandler>()
            .first { it.entityTypeId == PROJECT_PHOTO_SYNC_ENTITY_TYPE }
    }

    private val projectId = Uuid.parse("00000000-0000-0000-0000-000000000001")
    private val photoId = Uuid.parse("00000000-0000-0000-0002-000000000001")

    private fun createLocalPhoto(
        id: Uuid = photoId,
        url: String = "local/path/to/photo.jpg",
    ) = AppProjectPhotoData(
        id = id,
        projectId = projectId,
        url = url,
        description = "Test photo",
        updatedAt = Timestamp(10L),
    )

    private fun createRemotePhoto(
        id: Uuid = photoId,
        url: String = "https://storage.example.com/photo.jpg",
    ) = AppProjectPhotoData(
        id = id,
        projectId = projectId,
        url = url,
        description = "Test photo",
        updatedAt = Timestamp(10L),
    )

    @Test
    fun `uploads local file to remote and updates URL in database`() =
        runTest(testDispatcher) {
            val localPhoto = createLocalPhoto()
            fakeProjectPhotosDb.setPhoto(localPhoto)

            val result =
                handler.execute(
                    entityId = photoId,
                    operationType = SyncOperationType.UPSERT,
                )

            assertEquals(PushSyncOperationResult.Success, result)

            val savedPhotoResult = fakeProjectPhotosDb.getPhotoFlow(photoId).first()
            val savedPhoto = (savedPhotoResult as OfflineFirstDataResult.Success).data
            assertTrue(
                savedPhoto!!.url.startsWith("https://"),
                "Photo URL should be updated to remote URL, was: ${savedPhoto.url}",
            )
        }

    @Test
    fun `skips upload for already remote URLs`() =
        runTest(testDispatcher) {
            val remotePhoto = createRemotePhoto()
            fakeProjectPhotosDb.setPhoto(remotePhoto)

            val result =
                handler.execute(
                    entityId = photoId,
                    operationType = SyncOperationType.UPSERT,
                )

            assertEquals(PushSyncOperationResult.Success, result)
            assertTrue(
                fakeFileApi.uploadedFiles.isEmpty(),
                "Should not upload when URL is already remote",
            )
            assertTrue(
                fakeLocalFileStorage.readCalls.isEmpty(),
                "Should not read local file when URL is already remote",
            )
        }

    @Test
    fun `cleans up local file after successful upload`() =
        runTest(testDispatcher) {
            val localPhoto = createLocalPhoto()
            fakeProjectPhotosDb.setPhoto(localPhoto)

            handler.execute(
                entityId = photoId,
                operationType = SyncOperationType.UPSERT,
            )

            assertTrue(
                fakeLocalFileStorage.deletedPaths.isNotEmpty(),
                "Local file should be deleted after successful upload",
            )
        }

    @Test
    fun `does not delete local file when remote upload fails`() =
        runTest(testDispatcher) {
            val localPhoto = createLocalPhoto()
            fakeProjectPhotosDb.setPhoto(localPhoto)
            fakeFileApi.forcedFailure =
                OnlineDataResult.Failure.NetworkUnavailable

            handler.execute(
                entityId = photoId,
                operationType = SyncOperationType.UPSERT,
            )

            assertTrue(
                fakeLocalFileStorage.deletedPaths.isEmpty(),
                "Local file should not be deleted when upload fails",
            )

            val photoAfter =
                (fakeProjectPhotosDb.getPhotoFlow(photoId).first() as OfflineFirstDataResult.Success).data!!
            assertEquals(localPhoto.url, photoAfter.url)
        }

    @Test
    fun `returns failure when remote upload fails`() =
        runTest(testDispatcher) {
            val localPhoto = createLocalPhoto()
            fakeProjectPhotosDb.setPhoto(localPhoto)
            fakeFileApi.forcedFailure =
                OnlineDataResult.Failure.ProgrammerError(RuntimeException("Upload failed"))

            val result =
                handler.execute(
                    entityId = photoId,
                    operationType = SyncOperationType.UPSERT,
                )

            assertEquals(PushSyncOperationResult.Failure, result)
        }

    @Test
    fun `delete operation calls API`() =
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
    fun `delete operation returns failure when API fails`() =
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
