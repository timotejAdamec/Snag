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

package cz.adamec.timotej.snag.findings.be.app.impl.internal

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.foundation.common.UuidProvider
import cz.adamec.timotej.snag.feat.findings.be.model.BackendFindingPhotoData
import cz.adamec.timotej.snag.findings.be.app.api.SaveFindingPhotoUseCase
import cz.adamec.timotej.snag.findings.be.driven.test.seedTestFinding
import cz.adamec.timotej.snag.findings.be.ports.FindingsDb
import cz.adamec.timotej.snag.projects.be.driven.test.seedTestProject
import cz.adamec.timotej.snag.projects.be.ports.ProjectsDb
import cz.adamec.timotej.snag.structures.be.driven.test.seedTestStructure
import cz.adamec.timotej.snag.structures.be.ports.StructuresDb
import cz.adamec.timotej.snag.testinfra.be.BackendKoinInitializedTest
import cz.adamec.timotej.snag.users.be.driven.test.seedTestUser
import cz.adamec.timotej.snag.users.be.ports.UsersDb
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SaveFindingPhotoUseCaseImplTest : BackendKoinInitializedTest() {
    private val findingsDb: FindingsDb by inject()
    private val projectsDb: ProjectsDb by inject()
    private val structuresDb: StructuresDb by inject()
    private val usersDb: UsersDb by inject()
    private val useCase: SaveFindingPhotoUseCase by inject()

    private val projectId = UuidProvider.getUuid()
    private val structureId = UuidProvider.getUuid()
    private val findingId = UuidProvider.getUuid()
    private val photoId = UuidProvider.getUuid()

    private val backendFindingPhoto =
        BackendFindingPhotoData(
            id = photoId,
            findingId = findingId,
            url = "https://storage.test/photo.jpg",
            createdAt = Timestamp(10L),
        )

    private suspend fun seedParentEntities() {
        usersDb.seedTestUser()
        projectsDb.seedTestProject(id = projectId)
        structuresDb.seedTestStructure(
            id = structureId,
            projectId = projectId,
        )
        findingsDb.seedTestFinding(
            id = findingId,
            structureId = structureId,
        )
    }

    private suspend fun seedClosedProject() {
        usersDb.seedTestUser()
        projectsDb.seedTestProject(
            id = projectId,
            isClosed = true,
        )
        structuresDb.seedTestStructure(
            id = structureId,
            projectId = projectId,
        )
        findingsDb.seedTestFinding(
            id = findingId,
            structureId = structureId,
        )
    }

    @Test
    fun `saves new photo successfully`() =
        runTest(testDispatcher) {
            seedParentEntities()

            val result = useCase(backendFindingPhoto)

            assertNull(result)
        }

    @Test
    fun `rejects save when photo already exists`() =
        runTest(testDispatcher) {
            seedParentEntities()
            useCase(backendFindingPhoto)

            val duplicatePhoto =
                backendFindingPhoto.copy(
                    url = "https://storage.test/different.jpg",
                    createdAt = Timestamp(20L),
                )

            val result = useCase(duplicatePhoto)

            assertNotNull(result)
            assertEquals(backendFindingPhoto.id, result.id)
        }

    @Test
    fun `rejects save when project is closed`() =
        runTest(testDispatcher) {
            seedClosedProject()

            val result = useCase(backendFindingPhoto)

            assertNull(result)
        }
}
