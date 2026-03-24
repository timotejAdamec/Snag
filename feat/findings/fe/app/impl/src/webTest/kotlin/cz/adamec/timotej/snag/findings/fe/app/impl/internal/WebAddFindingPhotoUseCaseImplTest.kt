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

package cz.adamec.timotej.snag.findings.fe.app.impl.internal

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.foundation.common.TimestampProvider
import cz.adamec.timotej.snag.core.foundation.common.UuidProvider
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.core.storage.fe.RemoteFileStorage
import cz.adamec.timotej.snag.feat.findings.app.model.AppFindingPhoto
import cz.adamec.timotej.snag.findings.fe.app.api.AddFindingPhotoRequest
import cz.adamec.timotej.snag.findings.fe.app.api.WebAddFindingPhotoUseCase
import cz.adamec.timotej.snag.findings.fe.driven.test.FakeFindingPhotosDb
import cz.adamec.timotej.snag.sync.fe.app.api.EnqueueSyncSaveUseCase
import cz.adamec.timotej.snag.sync.fe.app.api.model.EnqueueSyncSaveRequest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class WebAddFindingPhotoUseCaseImplTest {
    private val projectId = Uuid.parse("00000000-0000-0000-0000-000000000001")
    private val findingId = Uuid.parse("00000000-0000-0000-0000-000000000002")
    private val nowTimestamp = Timestamp(1000L)
    private val photoBytes = byteArrayOf(1, 2, 3, 4)

    private val fakeFindingPhotosDb = FakeFindingPhotosDb()
    private val fakeRemoteFileStorage = FakeRemoteFileStorage()
    private val fakeEnqueueSyncSave = FakeEnqueueSyncSaveUseCase()

    private val fakeTimestampProvider =
        object : TimestampProvider {
            override fun getNowTimestamp(): Timestamp = nowTimestamp
        }

    private val useCase: WebAddFindingPhotoUseCase =
        WebAddFindingPhotoUseCaseImpl(
            remoteFileStorage = fakeRemoteFileStorage,
            findingPhotosDb = fakeFindingPhotosDb,
            enqueueSyncSaveUseCase = fakeEnqueueSyncSave,
            timestampProvider = fakeTimestampProvider,
            uuidProvider = UuidProvider,
        )

    private fun createRequest(fileName: String = "photo.jpg") =
        AddFindingPhotoRequest(
            bytes = photoBytes,
            fileName = fileName,
            findingId = findingId,
            projectId = projectId,
        )

    @Test
    fun `uploads photo to remote and saves to database`() =
        runTest {
            val result = useCase(createRequest())

            assertIs<OnlineDataResult.Success<Uuid>>(result)
            val photoId = result.data
            val dbResult = fakeFindingPhotosDb.getPhotoFlow(photoId).first()
            val savedPhoto = (dbResult as OfflineFirstDataResult.Success).data
            assertTrue(
                savedPhoto!!.url.startsWith("https://"),
                "Saved photo URL should be remote, was: ${savedPhoto.url}",
            )
            assertEquals(findingId, savedPhoto.findingId)
        }

    @Test
    fun `returns photo id on success`() =
        runTest {
            val result = useCase(createRequest())

            assertIs<OnlineDataResult.Success<Uuid>>(result)
            val photoId = result.data
            val dbResult = fakeFindingPhotosDb.getPhotoFlow(photoId).first()
            val savedPhoto = (dbResult as OfflineFirstDataResult.Success).data
            assertEquals(photoId, savedPhoto!!.id)
        }

    @Test
    fun `returns failure when upload fails`() =
        runTest {
            fakeRemoteFileStorage.forcedFailure =
                OnlineDataResult.Failure.ProgrammerError(RuntimeException("Upload failed"))

            val result = useCase(createRequest())

            assertIs<OnlineDataResult.Failure>(result)
        }

    @Test
    fun `enqueues sync save operation`() =
        runTest {
            val result = useCase(createRequest())

            assertIs<OnlineDataResult.Success<Uuid>>(result)
            assertEquals(1, fakeEnqueueSyncSave.enqueuedRequests.size)
            assertEquals(result.data, fakeEnqueueSyncSave.enqueuedRequests[0].entityId)
        }

    @Test
    fun `repeated invocations create separate photos`() =
        runTest {
            val result1 = useCase(createRequest())
            val result2 = useCase(createRequest())

            assertIs<OnlineDataResult.Success<Uuid>>(result1)
            assertIs<OnlineDataResult.Success<Uuid>>(result2)
            assertNotEquals(
                result1.data,
                result2.data,
                "Each invocation should produce a unique photo ID",
            )
            assertEquals(2, fakeEnqueueSyncSave.enqueuedRequests.size)
        }

    @Test
    fun `saved photo URL contains project and finding path`() =
        runTest {
            val result = useCase(createRequest())

            assertIs<OnlineDataResult.Success<Uuid>>(result)
            val dbResult = fakeFindingPhotosDb.getPhotoFlow(result.data).first()
            val savedPhoto = (dbResult as OfflineFirstDataResult.Success).data!!
            assertTrue(
                savedPhoto.url.contains(projectId.toString()),
                "URL should contain project ID",
            )
            assertTrue(
                savedPhoto.url.contains(findingId.toString()),
                "URL should contain finding ID",
            )
        }

    @Test
    fun `extracts file extension from fileName`() =
        runTest {
            val result = useCase(createRequest(fileName = "image.png"))

            assertIs<OnlineDataResult.Success<Uuid>>(result)
            val dbResult = fakeFindingPhotosDb.getPhotoFlow(result.data).first()
            val savedPhoto = (dbResult as OfflineFirstDataResult.Success).data!!
            assertTrue(
                savedPhoto.url.endsWith(".png"),
                "URL should end with .png extension, was: ${savedPhoto.url}",
            )
        }
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

private class FakeEnqueueSyncSaveUseCase : EnqueueSyncSaveUseCase {
    val enqueuedRequests = mutableListOf<EnqueueSyncSaveRequest>()

    override suspend fun invoke(request: EnqueueSyncSaveRequest) {
        enqueuedRequests.add(request)
    }
}
