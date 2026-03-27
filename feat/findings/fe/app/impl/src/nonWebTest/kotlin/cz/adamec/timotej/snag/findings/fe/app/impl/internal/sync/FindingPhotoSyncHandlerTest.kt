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

package cz.adamec.timotej.snag.findings.fe.app.impl.internal.sync

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.feat.findings.app.model.AppFindingData
import cz.adamec.timotej.snag.feat.findings.app.model.AppFindingPhotoData
import cz.adamec.timotej.snag.feat.findings.business.FindingType
import cz.adamec.timotej.snag.feat.structures.app.model.AppStructureData
import cz.adamec.timotej.snag.findings.fe.driven.test.FakeFindingPhotosApi
import cz.adamec.timotej.snag.findings.fe.driven.test.FakeFindingPhotosDb
import cz.adamec.timotej.snag.findings.fe.driven.test.FakeFindingsDb
import cz.adamec.timotej.snag.lib.storage.fe.test.FakeFileApi
import cz.adamec.timotej.snag.lib.storage.fe.test.FakeLocalFileStorage
import cz.adamec.timotej.snag.structures.fe.driven.test.FakeStructuresDb
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
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class FindingPhotoSyncHandlerTest : FrontendKoinInitializedTest() {
    private val fakeFindingPhotosDb: FakeFindingPhotosDb by inject()
    private val fakeFindingPhotosApi: FakeFindingPhotosApi by inject()
    private val fakeFindingsDb: FakeFindingsDb by inject()
    private val fakeStructuresDb: FakeStructuresDb by inject()
    private val fakeFileApi: FakeFileApi by inject()
    private val fakeLocalFileStorage: FakeLocalFileStorage by inject()

    private val handler: PushSyncOperationHandler by lazy {
        getKoin()
            .getAll<PushSyncOperationHandler>()
            .first { it.entityTypeId == FINDING_PHOTO_SYNC_ENTITY_TYPE }
    }

    private val projectId = Uuid.parse("00000000-0000-0000-0000-000000000001")
    private val structureId = Uuid.parse("00000000-0000-0000-0000-000000000002")
    private val findingId = Uuid.parse("00000000-0000-0000-0000-000000000003")
    private val photoId = Uuid.parse("00000000-0000-0000-0000-000000000004")
    private val nowTimestamp = Timestamp(1000L)

    private fun setupFindingAndStructure() {
        fakeFindingsDb.setFinding(
            AppFindingData(
                id = findingId,
                structureId = structureId,
                name = "Test Finding",
                description = null,
                type = FindingType.Classic(),
                coordinates = emptySet(),
                updatedAt = nowTimestamp,
            ),
        )
        fakeStructuresDb.setStructure(
            AppStructureData(
                id = structureId,
                projectId = projectId,
                name = "Test Structure",
                floorPlanUrl = null,
                updatedAt = nowTimestamp,
            ),
        )
    }

    private fun createLocalPhoto(
        id: Uuid = photoId,
        url: String = "local/path/to/photo.jpg",
    ) = AppFindingPhotoData(
        id = id,
        findingId = findingId,
        url = url,
        createdAt = nowTimestamp,
    )

    private fun createRemotePhoto(
        id: Uuid = photoId,
        url: String = "https://storage.example.com/photo.jpg",
    ) = AppFindingPhotoData(
        id = id,
        findingId = findingId,
        url = url,
        createdAt = nowTimestamp,
    )

    @Test
    fun `uploads local file to remote and updates URL in database`() =
        runTest(testDispatcher) {
            val localPhoto = createLocalPhoto()
            fakeFindingPhotosDb.setPhoto(localPhoto)
            setupFindingAndStructure()

            val result =
                handler.execute(
                    entityId = photoId,
                    operationType = SyncOperationType.UPSERT,
                )

            assertEquals(PushSyncOperationResult.Success, result)

            val savedPhotoResult = fakeFindingPhotosDb.getPhotoFlow(photoId).first()
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
            fakeFindingPhotosDb.setPhoto(remotePhoto)
            setupFindingAndStructure()

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
    fun `sends photo to API after uploading to remote storage`() =
        runTest(testDispatcher) {
            val localPhoto = createLocalPhoto()
            fakeFindingPhotosDb.setPhoto(localPhoto)
            setupFindingAndStructure()

            handler.execute(
                entityId = photoId,
                operationType = SyncOperationType.UPSERT,
            )

            assertTrue(
                fakeFileApi.uploadedFiles.isNotEmpty(),
                "Should have uploaded file to remote storage",
            )
        }

    @Test
    fun `returns failure when remote upload fails`() =
        runTest(testDispatcher) {
            val localPhoto = createLocalPhoto()
            fakeFindingPhotosDb.setPhoto(localPhoto)
            setupFindingAndStructure()
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
    fun `does not delete local file when remote upload fails`() =
        runTest(testDispatcher) {
            val localPhoto = createLocalPhoto()
            fakeFindingPhotosDb.setPhoto(localPhoto)
            setupFindingAndStructure()
            fakeFileApi.forcedFailure =
                OnlineDataResult.Failure.NetworkUnavailable

            handler.execute(
                entityId = photoId,
                operationType = SyncOperationType.UPSERT,
            )

            // Local file should NOT have been deleted
            assertTrue(
                fakeLocalFileStorage.deletedPaths.isEmpty(),
                "Local file should not be deleted when upload fails",
            )

            // Photo should still have local URL in DB
            val photoAfter =
                (fakeFindingPhotosDb.getPhotoFlow(photoId).first() as OfflineFirstDataResult.Success).data!!
            assertEquals(localPhoto.url, photoAfter.url)
        }

    @Test
    fun `upload failure followed by API failure keeps local file intact`() =
        runTest(testDispatcher) {
            val localPhoto = createLocalPhoto()
            fakeFindingPhotosDb.setPhoto(localPhoto)
            setupFindingAndStructure()

            // First: remote upload fails
            fakeFileApi.forcedFailure =
                OnlineDataResult.Failure.NetworkUnavailable

            val result1 =
                handler.execute(
                    entityId = photoId,
                    operationType = SyncOperationType.UPSERT,
                )
            assertEquals(PushSyncOperationResult.Failure, result1)
            assertTrue(fakeLocalFileStorage.deletedPaths.isEmpty())

            // Second: upload succeeds but API fails
            fakeFileApi.forcedFailure = null
            fakeFindingPhotosApi.forcedFailure =
                OnlineDataResult.Failure.NetworkUnavailable

            val result2 =
                handler.execute(
                    entityId = photoId,
                    operationType = SyncOperationType.UPSERT,
                )
            assertEquals(PushSyncOperationResult.Failure, result2)

            // Third: both succeed
            fakeFindingPhotosApi.forcedFailure = null

            val result3 =
                handler.execute(
                    entityId = photoId,
                    operationType = SyncOperationType.UPSERT,
                )
            assertEquals(PushSyncOperationResult.Success, result3)

            // Photo should now have remote URL
            val photoAfter =
                (fakeFindingPhotosDb.getPhotoFlow(photoId).first() as OfflineFirstDataResult.Success).data!!
            assertTrue(photoAfter.url.startsWith("http"))
        }

    @Test
    fun `returns entity not found when photo is not in database`() =
        runTest(testDispatcher) {
            val missingId = Uuid.parse("00000000-0000-0000-0000-000000000099")

            val result =
                handler.execute(
                    entityId = missingId,
                    operationType = SyncOperationType.UPSERT,
                )

            assertEquals(PushSyncOperationResult.EntityNotFound, result)
        }

    @Test
    fun `delete operation calls API with scopeId as findingId`() =
        runTest(testDispatcher) {
            val result =
                handler.execute(
                    entityId = photoId,
                    operationType = SyncOperationType.DELETE,
                    scopeId = findingId,
                )

            assertEquals(PushSyncOperationResult.Success, result)
        }

    @Test
    fun `delete operation returns failure when API fails`() =
        runTest(testDispatcher) {
            fakeFindingPhotosApi.forcedFailure =
                OnlineDataResult.Failure.ProgrammerError(RuntimeException("API error"))

            val result =
                handler.execute(
                    entityId = photoId,
                    operationType = SyncOperationType.DELETE,
                    scopeId = findingId,
                )

            assertEquals(PushSyncOperationResult.Failure, result)
        }

    @Test
    fun `sends photo with remote URL to API`() =
        runTest(testDispatcher) {
            val remotePhoto = createRemotePhoto()
            fakeFindingPhotosDb.setPhoto(remotePhoto)
            setupFindingAndStructure()

            val result =
                handler.execute(
                    entityId = photoId,
                    operationType = SyncOperationType.UPSERT,
                )

            assertEquals(PushSyncOperationResult.Success, result)
        }

    @Test
    fun `returns failure when finding cannot be resolved for project ID`() =
        runTest(testDispatcher) {
            val localPhoto = createLocalPhoto()
            fakeFindingPhotosDb.setPhoto(localPhoto)
            // Do not set up finding/structure, so resolveProjectId returns null

            val result =
                handler.execute(
                    entityId = photoId,
                    operationType = SyncOperationType.UPSERT,
                )

            assertIs<PushSyncOperationResult>(result)
            assertEquals(PushSyncOperationResult.Failure, result)
        }

    @Test
    fun `succeeds on third attempt after two API failures`() =
        runTest(testDispatcher) {
            val localPhoto = createLocalPhoto()
            fakeFindingPhotosDb.setPhoto(localPhoto)
            setupFindingAndStructure()
            fakeFindingPhotosApi.forcedFailure =
                OnlineDataResult.Failure.NetworkUnavailable

            // First attempt — file uploads to GCS but API push fails
            val result1 =
                handler.execute(
                    entityId = photoId,
                    operationType = SyncOperationType.UPSERT,
                )
            assertEquals(PushSyncOperationResult.Failure, result1)

            // File was uploaded to GCS and URL was swapped to remote in DB,
            // but the API push failed — so sync is not complete
            val photoAfterFirst =
                (fakeFindingPhotosDb.getPhotoFlow(photoId).first() as OfflineFirstDataResult.Success).data!!
            assertTrue(photoAfterFirst.url.startsWith("http"))

            // Second attempt — URL is now remote, handler retries API push but still fails
            val result2 =
                handler.execute(
                    entityId = photoId,
                    operationType = SyncOperationType.UPSERT,
                )
            assertEquals(PushSyncOperationResult.Failure, result2)

            // Third attempt — API succeeds
            fakeFindingPhotosApi.forcedFailure = null
            val result3 =
                handler.execute(
                    entityId = photoId,
                    operationType = SyncOperationType.UPSERT,
                )
            assertEquals(PushSyncOperationResult.Success, result3)

            // Photo still has remote URL
            val photoAfterThird =
                (fakeFindingPhotosDb.getPhotoFlow(photoId).first() as OfflineFirstDataResult.Success).data!!
            assertTrue(photoAfterThird.url.startsWith("http"))
        }
}
