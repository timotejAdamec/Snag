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

package cz.adamec.timotej.snag.feat.inspections.be.app.impl.internal

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.foundation.common.UuidProvider
import cz.adamec.timotej.snag.feat.inspections.be.app.api.DeleteInspectionUseCase
import cz.adamec.timotej.snag.feat.inspections.be.app.api.model.DeleteInspectionRequest
import cz.adamec.timotej.snag.feat.inspections.be.model.BackendInspectionData
import cz.adamec.timotej.snag.feat.inspections.be.ports.InspectionsDb
import cz.adamec.timotej.snag.projects.be.model.BackendProjectData
import cz.adamec.timotej.snag.projects.be.ports.ProjectsDb
import cz.adamec.timotej.snag.testinfra.be.BackendKoinInitializedTest
import cz.adamec.timotej.snag.users.be.model.BackendUserData
import cz.adamec.timotej.snag.users.be.ports.UsersDb
import cz.adamec.timotej.snag.users.business.UserRole
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.uuid.Uuid

class DeleteInspectionUseCaseImplTest : BackendKoinInitializedTest() {
    private val inspectionsDb: InspectionsDb by inject()
    private val projectsDb: ProjectsDb by inject()
    private val usersDb: UsersDb by inject()
    private val useCase: DeleteInspectionUseCase by inject()

    private val projectId = UuidProvider.getUuid()
    private val inspectionId = UuidProvider.getUuid()

    private val backendInspection =
        BackendInspectionData(
            id = inspectionId,
            projectId = projectId,
            startedAt = null,
            endedAt = null,
            participants = null,
            climate = null,
            note = null,
            updatedAt = Timestamp(10L),
        )

    private suspend fun seedTestUser() {
        usersDb.saveUser(
            BackendUserData(
                id = TEST_USER_ID,
                entraId = "test-entra",
                email = "test@example.com",
                role = UserRole.ADMINISTRATOR,
                updatedAt = Timestamp(1L),
            ),
        )
    }

    private suspend fun seedClosedProject() {
        seedTestUser()
        projectsDb.saveProject(
            BackendProjectData(
                id = projectId,
                name = "Test Project",
                address = "Test Address",
                creatorId = TEST_USER_ID,
                isClosed = true,
                updatedAt = Timestamp(1L),
            ),
        )
    }

    @Test
    fun `returns existing entity when project is closed`() =
        runTest(testDispatcher) {
            seedClosedProject()
            inspectionsDb.saveInspection(backendInspection)

            val result =
                useCase(
                    DeleteInspectionRequest(
                        inspectionId = inspectionId,
                        deletedAt = Timestamp(20L),
                    ),
                )

            assertEquals(backendInspection, result)
            val stored = inspectionsDb.getInspection(inspectionId)
            assertNull(stored?.deletedAt)
        }

    companion object {
        private val TEST_USER_ID = Uuid.parse("00000000-0000-0000-0000-000000000042")
    }
}
