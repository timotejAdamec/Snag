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
import cz.adamec.timotej.snag.projects.app.model.AppProjectPhoto
import cz.adamec.timotej.snag.projects.app.model.AppProjectPhotoData
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectPhotosApi
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectPhotosDb
import cz.adamec.timotej.snag.projects.fe.ports.ProjectPhotoSyncResult
import cz.adamec.timotej.snag.sync.fe.app.api.handler.PullSyncOperationHandler
import cz.adamec.timotej.snag.sync.fe.app.api.handler.PullSyncOperationResult
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.koin.mp.KoinPlatform.getKoin
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectPhotoPullSyncHandlerTest : FrontendKoinInitializedTest() {
    private val fakeProjectPhotosApi: FakeProjectPhotosApi by inject()
    private val fakeProjectPhotosDb: FakeProjectPhotosDb by inject()

    private val handler: PullSyncOperationHandler by lazy {
        getKoin()
            .getAll<PullSyncOperationHandler>()
            .first { it.entityTypeId == PROJECT_PHOTO_SYNC_ENTITY_TYPE }
    }

    private val projectId = Uuid.parse("00000000-0000-0000-0000-000000000001")
    private val photoId = Uuid.parse("00000000-0000-0000-0002-000000000001")

    @Test
    fun `saves updated photos from API to DB`() =
        runTest(testDispatcher) {
            val photo =
                AppProjectPhotoData(
                    id = photoId,
                    projectId = projectId,
                    url = "https://example.com/photo.jpg",
                    description = "Synced photo",
                    updatedAt = Timestamp(10L),
                )
            fakeProjectPhotosApi.modifiedSinceResults =
                listOf(ProjectPhotoSyncResult.Updated(photo = photo))

            val result = handler.execute(scopeId = projectId)

            assertEquals(PullSyncOperationResult.Success, result)
            val dbResult = fakeProjectPhotosDb.getPhotoFlow(photoId).first()
            assertIs<OfflineFirstDataResult.Success<AppProjectPhoto?>>(dbResult)
            assertEquals(photo, dbResult.data)
        }

    @Test
    fun `deletes photos marked as deleted from DB`() =
        runTest(testDispatcher) {
            val photo =
                AppProjectPhotoData(
                    id = photoId,
                    projectId = projectId,
                    url = "https://example.com/photo.jpg",
                    description = "To be deleted",
                    updatedAt = Timestamp(1L),
                )
            fakeProjectPhotosDb.setPhoto(photo)

            fakeProjectPhotosApi.modifiedSinceResults =
                listOf(ProjectPhotoSyncResult.Deleted(id = photoId))

            val result = handler.execute(scopeId = projectId)

            assertEquals(PullSyncOperationResult.Success, result)
            val dbResult = fakeProjectPhotosDb.getPhotoFlow(photoId).first()
            assertIs<OfflineFirstDataResult.Success<AppProjectPhoto?>>(dbResult)
            assertNull(dbResult.data)
        }

    @Test
    fun `returns failure when API fails`() =
        runTest(testDispatcher) {
            fakeProjectPhotosApi.forcedFailure = OnlineDataResult.Failure.NetworkUnavailable

            val result = handler.execute(scopeId = projectId)

            assertEquals(PullSyncOperationResult.Failure, result)
        }
}
