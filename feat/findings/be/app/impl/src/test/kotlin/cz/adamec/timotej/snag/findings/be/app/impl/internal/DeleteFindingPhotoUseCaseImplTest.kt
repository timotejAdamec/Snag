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
import cz.adamec.timotej.snag.findings.be.app.api.DeleteFindingPhotoUseCase
import cz.adamec.timotej.snag.findings.be.app.api.model.DeleteFindingPhotoRequest
import cz.adamec.timotej.snag.findings.be.driven.test.seedTestFinding
import cz.adamec.timotej.snag.findings.be.ports.FindingPhotosDb
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
import kotlin.test.assertNull

class DeleteFindingPhotoUseCaseImplTest : BackendKoinInitializedTest() {
    private val findingPhotosDb: FindingPhotosDb by inject()
    private val findingsDb: FindingsDb by inject()
    private val projectsDb: ProjectsDb by inject()
    private val structuresDb: StructuresDb by inject()
    private val usersDb: UsersDb by inject()
    private val useCase: DeleteFindingPhotoUseCase by inject()

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

    @Test
    fun `deletes photo successfully`() =
        runTest(testDispatcher) {
            seedParentEntities()
            findingPhotosDb.savePhoto(backendFindingPhoto)

            val result =
                useCase(
                    DeleteFindingPhotoRequest(
                        photoId = photoId,
                        deletedAt = Timestamp(20L),
                    ),
                )

            assertNull(result)
        }

    @Test
    fun `returns null for non-existent photo`() =
        runTest(testDispatcher) {
            val nonExistentId = UuidProvider.getUuid()

            val result =
                useCase(
                    DeleteFindingPhotoRequest(
                        photoId = nonExistentId,
                        deletedAt = Timestamp(20L),
                    ),
                )

            assertNull(result)
        }
}
