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

package cz.adamec.timotej.snag.projects.fe.app.impl.internal

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.projects.app.model.AppProjectPhotoData
import cz.adamec.timotej.snag.projects.fe.app.api.DeleteProjectPhotoUseCase
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.sync.PROJECT_PHOTO_SYNC_ENTITY_TYPE
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectPhotosDb
import cz.adamec.timotej.snag.sync.fe.driven.test.FakeSyncQueue
import cz.adamec.timotej.snag.sync.fe.model.SyncOperationType
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class DeleteProjectPhotoUseCaseImplTest : FrontendKoinInitializedTest() {
    private val fakeProjectPhotosDb: FakeProjectPhotosDb by inject()
    private val fakeSyncQueue: FakeSyncQueue by inject()

    private val useCase: DeleteProjectPhotoUseCase by inject()

    private val projectId = Uuid.parse("00000000-0000-0000-0000-000000000001")
    private val photoId = Uuid.parse("00000000-0000-0000-0002-000000000001")

    private fun createPhoto(id: Uuid = photoId) =
        AppProjectPhotoData(
            id = id,
            projectId = projectId,
            url = "https://example.com/photo.jpg",
            description = "Test photo",
            updatedAt = Timestamp(1L),
        )

    @Test
    fun `deletes photo from DB and enqueues sync delete`() =
        runTest(testDispatcher) {
            fakeProjectPhotosDb.setPhoto(createPhoto())

            val result = useCase(photoId)

            assertIs<OfflineFirstDataResult.Success<Unit>>(result)

            val photoResult = fakeProjectPhotosDb.getPhotoFlow(photoId).first()
            assertIs<OfflineFirstDataResult.Success<*>>(photoResult)
            assertNull(photoResult.data)

            val pending = fakeSyncQueue.getAllPending()
            assertEquals(1, pending.size)
            assertEquals(PROJECT_PHOTO_SYNC_ENTITY_TYPE, pending[0].entityTypeId)
            assertEquals(photoId, pending[0].entityId)
            assertEquals(SyncOperationType.DELETE, pending[0].operationType)
            assertEquals(projectId, pending[0].scopeId)
        }

    @Test
    fun `returns error when DB fails to read photo`() =
        runTest(testDispatcher) {
            fakeProjectPhotosDb.forcedFailure =
                OfflineFirstDataResult.ProgrammerError(Exception("DB error"))

            val result = useCase(photoId)

            assertIs<OfflineFirstDataResult.ProgrammerError>(result)
            assertTrue(fakeSyncQueue.getAllPending().isEmpty())
        }
}
