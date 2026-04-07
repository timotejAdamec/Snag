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
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.projects.app.model.AppProjectPhoto
import cz.adamec.timotej.snag.projects.app.model.AppProjectPhotoData
import cz.adamec.timotej.snag.projects.fe.app.api.CascadeRestoreLocalProjectPhotosByProjectIdUseCase
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectPhotosApi
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectPhotosDb
import cz.adamec.timotej.snag.projects.fe.ports.ProjectPhotoSyncResult
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class CascadeRestoreLocalProjectPhotosByProjectIdUseCaseImplTest : FrontendKoinInitializedTest() {
    private val fakeProjectPhotosApi: FakeProjectPhotosApi by inject()
    private val fakeProjectPhotosDb: FakeProjectPhotosDb by inject()

    private val useCase: CascadeRestoreLocalProjectPhotosByProjectIdUseCase by inject()

    private val projectId = Uuid.parse("00000000-0000-0000-0000-000000000001")
    private val photoId1 = Uuid.parse("00000000-0000-0000-0002-000000000001")
    private val photoId2 = Uuid.parse("00000000-0000-0000-0002-000000000002")

    private fun createPhoto(
        id: Uuid,
        projectId: Uuid,
    ) = AppProjectPhotoData(
        id = id,
        projectId = projectId,
        url = "https://example.com/$id.jpg",
        description = "Photo $id",
        updatedAt = Timestamp(1L),
    )

    @Test
    fun `restores photos from API to local DB`() =
        runTest(testDispatcher) {
            val photo1 = createPhoto(id = photoId1, projectId = projectId)
            val photo2 = createPhoto(id = photoId2, projectId = projectId)
            fakeProjectPhotosApi.modifiedSinceResults =
                listOf(
                    ProjectPhotoSyncResult.Updated(photo = photo1),
                    ProjectPhotoSyncResult.Updated(photo = photo2),
                )

            useCase(projectId)

            val result = fakeProjectPhotosDb.getPhotosFlow(projectId).first()
            assertIs<OfflineFirstDataResult.Success<List<AppProjectPhoto>>>(result)
            assertEquals(2, result.data.size)
        }

    @Test
    fun `clears stale local photos before restoring`() =
        runTest(testDispatcher) {
            val stalePhoto = createPhoto(id = photoId1, projectId = projectId)
            fakeProjectPhotosDb.setPhoto(stalePhoto)

            val freshPhoto = createPhoto(id = photoId2, projectId = projectId)
            fakeProjectPhotosApi.modifiedSinceResults =
                listOf(ProjectPhotoSyncResult.Updated(photo = freshPhoto))

            useCase(projectId)

            val result = fakeProjectPhotosDb.getPhotosFlow(projectId).first()
            assertIs<OfflineFirstDataResult.Success<List<AppProjectPhoto>>>(result)
            assertEquals(1, result.data.size)
            assertEquals(photoId2, result.data[0].id)
        }

    @Test
    fun `does not crash when API fails`() =
        runTest(testDispatcher) {
            fakeProjectPhotosApi.forcedFailure =
                OnlineDataResult.Failure.ProgrammerError(Exception("API error"))

            useCase(projectId)

            val result = fakeProjectPhotosDb.getPhotosFlow(projectId).first()
            assertIs<OfflineFirstDataResult.Success<List<AppProjectPhoto>>>(result)
            assertTrue(result.data.isEmpty())
        }
}
