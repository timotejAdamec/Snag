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

package cz.adamec.timotej.snag.projects.be.app.impl.internal

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.projects.be.app.api.SaveProjectPhotoUseCase
import cz.adamec.timotej.snag.projects.be.driven.test.TEST_PROJECT_ID
import cz.adamec.timotej.snag.projects.be.driven.test.TEST_PROJECT_PHOTO_ID
import cz.adamec.timotej.snag.projects.be.driven.test.seedTestProject
import cz.adamec.timotej.snag.projects.be.driven.test.seedTestProjectPhoto
import cz.adamec.timotej.snag.projects.be.model.BackendProjectPhotoData
import cz.adamec.timotej.snag.projects.be.ports.ProjectPhotosDb
import cz.adamec.timotej.snag.projects.be.ports.ProjectsDb
import cz.adamec.timotej.snag.testinfra.be.BackendKoinInitializedTest
import cz.adamec.timotej.snag.users.be.driven.test.seedTestUser
import cz.adamec.timotej.snag.users.be.ports.UsersDb
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SaveProjectPhotoUseCaseImplTest : BackendKoinInitializedTest() {
    private val projectsDb: ProjectsDb by inject()
    private val projectPhotosDb: ProjectPhotosDb by inject()
    private val usersDb: UsersDb by inject()
    private val useCase: SaveProjectPhotoUseCase by inject()

    private val photo =
        BackendProjectPhotoData(
            id = TEST_PROJECT_PHOTO_ID,
            projectId = TEST_PROJECT_ID,
            url = "https://example.com/photo.jpg",
            description = "Test description",
            updatedAt = Timestamp(10L),
        )

    private suspend fun seedPrerequisites(isClosed: Boolean = false) {
        usersDb.seedTestUser()
        projectsDb.seedTestProject(isClosed = isClosed)
    }

    @Test
    fun `saves photo when project exists and is editable`() =
        runTest(testDispatcher) {
            seedPrerequisites()

            val result = useCase(photo)

            assertNull(result)
        }

    @Test
    fun `returns rejected photo when server has newer updatedAt`() =
        runTest(testDispatcher) {
            seedPrerequisites()
            projectPhotosDb.seedTestProjectPhoto(updatedAt = Timestamp(20L))

            val result = useCase(photo)

            assertNotNull(result)
            assertEquals(Timestamp(20L), result.updatedAt)
        }

    @Test
    fun `returns null when project is closed`() =
        runTest(testDispatcher) {
            seedPrerequisites(isClosed = true)

            val result = useCase(photo)

            assertNull(result)
        }

    @Test
    fun `restores soft-deleted photo with newer timestamp`() =
        runTest(testDispatcher) {
            seedPrerequisites()
            projectPhotosDb.seedTestProjectPhoto(
                updatedAt = Timestamp(5L),
                deletedAt = Timestamp(8L),
            )

            val restoredPhoto =
                photo.copy(
                    description = "Restored",
                    updatedAt = Timestamp(15L),
                )

            val result = useCase(restoredPhoto)

            assertNull(result)
            val stored =
                projectPhotosDb
                    .getPhotosModifiedSince(
                        projectId = TEST_PROJECT_ID,
                        since = Timestamp(0L),
                    ).first { it.id == TEST_PROJECT_PHOTO_ID }
            assertNull(stored.deletedAt)
            assertEquals("Restored", stored.description)
        }
}
