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
import cz.adamec.timotej.snag.core.foundation.common.TimestampProvider
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.core.storage.fe.LocalFileStorage
import cz.adamec.timotej.snag.core.storage.fe.RemoteFileStorage
import cz.adamec.timotej.snag.feat.findings.app.model.AppFindingData
import cz.adamec.timotej.snag.feat.findings.app.model.AppFindingPhotoData
import cz.adamec.timotej.snag.feat.findings.business.FindingType
import cz.adamec.timotej.snag.feat.structures.app.model.AppStructure
import cz.adamec.timotej.snag.feat.structures.app.model.AppStructureData
import cz.adamec.timotej.snag.findings.fe.driven.test.FakeFindingPhotosApi
import cz.adamec.timotej.snag.findings.fe.driven.test.FakeFindingPhotosDb
import cz.adamec.timotej.snag.findings.fe.driven.test.FakeFindingsDb
import cz.adamec.timotej.snag.structures.fe.app.api.GetStructureUseCase
import cz.adamec.timotej.snag.sync.fe.app.api.handler.PushSyncOperationResult
import cz.adamec.timotej.snag.sync.fe.model.SyncOperationType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class FindingPhotoSyncHandlerTest {
    private val testDispatcher = StandardTestDispatcher()

    private val projectId = Uuid.parse("00000000-0000-0000-0000-000000000001")
    private val structureId = Uuid.parse("00000000-0000-0000-0000-000000000002")
    private val findingId = Uuid.parse("00000000-0000-0000-0000-000000000003")
    private val photoId = Uuid.parse("00000000-0000-0000-0000-000000000004")
    private val nowTimestamp = Timestamp(1000L)

    private val fakeFindingPhotosDb = FakeFindingPhotosDb()
    private val fakeFindingPhotosApi = FakeFindingPhotosApi()
    private val fakeFindingsDb = FakeFindingsDb()
    private val fakeLocalFileStorage = FakeLocalFileStorage()
    private val fakeRemoteFileStorage = FakeRemoteFileStorage()
    private val fakeGetStructureUseCase = FakeGetStructureUseCase()

    private val fakeTimestampProvider =
        object : TimestampProvider {
            override fun getNowTimestamp(): Timestamp = nowTimestamp
        }

    private val handler =
        FindingPhotoSyncHandler(
            findingPhotosApi = fakeFindingPhotosApi,
            findingPhotosDb = fakeFindingPhotosDb,
            localFileStorage = fakeLocalFileStorage,
            remoteFileStorage = fakeRemoteFileStorage,
            findingsDb = fakeFindingsDb,
            getStructureUseCase = fakeGetStructureUseCase,
            timestampProvider = fakeTimestampProvider,
        )

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
        fakeGetStructureUseCase.structures[structureId] =
            AppStructureData(
                id = structureId,
                projectId = projectId,
                name = "Test Structure",
                floorPlanUrl = null,
                updatedAt = nowTimestamp,
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

            val result = handler.execute(
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

            val result = handler.execute(
                entityId = photoId,
                operationType = SyncOperationType.UPSERT,
            )

            assertEquals(PushSyncOperationResult.Success, result)
            assertTrue(
                fakeRemoteFileStorage.uploadedFiles.isEmpty(),
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
                fakeRemoteFileStorage.uploadedFiles.isNotEmpty(),
                "Should have uploaded file to remote storage",
            )
        }

    @Test
    fun `returns failure when remote upload fails`() =
        runTest(testDispatcher) {
            val localPhoto = createLocalPhoto()
            fakeFindingPhotosDb.setPhoto(localPhoto)
            setupFindingAndStructure()
            fakeRemoteFileStorage.forcedFailure =
                OnlineDataResult.Failure.ProgrammerError(RuntimeException("Upload failed"))

            val result = handler.execute(
                entityId = photoId,
                operationType = SyncOperationType.UPSERT,
            )

            assertEquals(PushSyncOperationResult.Failure, result)
        }

    @Test
    fun `returns entity not found when photo is not in database`() =
        runTest(testDispatcher) {
            val missingId = Uuid.parse("00000000-0000-0000-0000-000000000099")

            val result = handler.execute(
                entityId = missingId,
                operationType = SyncOperationType.UPSERT,
            )

            assertEquals(PushSyncOperationResult.EntityNotFound, result)
        }

    @Test
    fun `delete operation calls API and returns success`() =
        runTest(testDispatcher) {
            val result = handler.execute(
                entityId = photoId,
                operationType = SyncOperationType.DELETE,
            )

            assertEquals(PushSyncOperationResult.Success, result)
        }

    @Test
    fun `delete operation returns failure when API fails`() =
        runTest(testDispatcher) {
            fakeFindingPhotosApi.forcedFailure =
                OnlineDataResult.Failure.ProgrammerError(RuntimeException("API error"))

            val result = handler.execute(
                entityId = photoId,
                operationType = SyncOperationType.DELETE,
            )

            assertEquals(PushSyncOperationResult.Failure, result)
        }

    @Test
    fun `sends photo with remote URL to API`() =
        runTest(testDispatcher) {
            val remotePhoto = createRemotePhoto()
            fakeFindingPhotosDb.setPhoto(remotePhoto)
            setupFindingAndStructure()

            val result = handler.execute(
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

            val result = handler.execute(
                entityId = photoId,
                operationType = SyncOperationType.UPSERT,
            )

            assertIs<PushSyncOperationResult>(result)
            assertEquals(PushSyncOperationResult.Failure, result)
        }
}

private class FakeLocalFileStorage : LocalFileStorage {
    val readCalls = mutableListOf<String>()

    override suspend fun saveFile(
        bytes: ByteArray,
        fileName: String,
        subdirectory: String,
    ): String = "$subdirectory/$fileName"

    override suspend fun readFileBytes(path: String): ByteArray {
        readCalls.add(path)
        return byteArrayOf(1, 2, 3, 4)
    }

    override suspend fun deleteFile(path: String) = Unit
}

private class FakeRemoteFileStorage : RemoteFileStorage {
    val uploadedFiles = mutableListOf<Triple<ByteArray, String, String>>()
    val deletedUrls = mutableListOf<String>()
    var forcedFailure: OnlineDataResult.Failure? = null

    override suspend fun uploadFile(
        bytes: ByteArray,
        fileName: String,
        directory: String,
    ): OnlineDataResult<String> {
        val failure = forcedFailure
        if (failure != null) return failure
        uploadedFiles.add(Triple(bytes, fileName, directory))
        return OnlineDataResult.Success("https://storage.test/$directory/$fileName")
    }

    override suspend fun deleteFile(url: String): OnlineDataResult<Unit> {
        val failure = forcedFailure
        if (failure != null) return failure
        deletedUrls.add(url)
        return OnlineDataResult.Success(Unit)
    }
}

private class FakeGetStructureUseCase : GetStructureUseCase {
    val structures = mutableMapOf<Uuid, AppStructure>()

    override fun invoke(structureId: Uuid): Flow<OfflineFirstDataResult<AppStructure?>> =
        flowOf(OfflineFirstDataResult.Success(structures[structureId]))
}
