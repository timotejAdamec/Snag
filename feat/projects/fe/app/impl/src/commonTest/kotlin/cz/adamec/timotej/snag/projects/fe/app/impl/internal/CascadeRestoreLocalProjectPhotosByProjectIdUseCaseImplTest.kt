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
import cz.adamec.timotej.snag.projects.app.model.AppProjectPhoto
import cz.adamec.timotej.snag.projects.app.model.AppProjectPhotoData
import cz.adamec.timotej.snag.projects.fe.app.api.CascadeRestoreLocalProjectPhotosByProjectIdUseCase
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectPhotosDb
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class CascadeRestoreLocalProjectPhotosByProjectIdUseCaseImplTest : FrontendKoinInitializedTest() {
    private val fakeProjectPhotosDb: FakeProjectPhotosDb by inject()

    private val useCase: CascadeRestoreLocalProjectPhotosByProjectIdUseCase by inject()

    private val projectId = Uuid.parse("00000000-0000-0000-0000-000000000001")
    private val photoId = Uuid.parse("00000000-0000-0000-0002-000000000001")

    @Test
    fun `clears local photos for project`() =
        runTest(testDispatcher) {
            fakeProjectPhotosDb.setPhoto(
                AppProjectPhotoData(
                    id = photoId,
                    projectId = projectId,
                    url = "https://example.com/photo.jpg",
                    description = "Photo",
                    updatedAt = Timestamp(1L),
                ),
            )

            useCase(projectId)

            val result = fakeProjectPhotosDb.getPhotosFlow(projectId).first()
            assertIs<OfflineFirstDataResult.Success<List<AppProjectPhoto>>>(result)
            assertTrue(result.data.isEmpty())
        }
}
