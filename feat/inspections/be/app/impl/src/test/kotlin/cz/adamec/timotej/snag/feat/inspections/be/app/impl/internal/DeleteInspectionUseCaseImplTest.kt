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

import cz.adamec.timotej.snag.feat.inspections.be.app.api.DeleteInspectionUseCase
import cz.adamec.timotej.snag.feat.inspections.be.app.api.model.DeleteInspectionRequest
import cz.adamec.timotej.snag.feat.inspections.be.model.BackendInspection
import cz.adamec.timotej.snag.feat.inspections.be.ports.InspectionsDb
import cz.adamec.timotej.snag.feat.inspections.business.Inspection
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.common.UuidProvider
import cz.adamec.timotej.snag.projects.be.model.BackendProject
import cz.adamec.timotej.snag.projects.be.ports.ProjectsDb
import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.testinfra.be.BackendKoinInitializedTest
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DeleteInspectionUseCaseImplTest : BackendKoinInitializedTest() {
    private val inspectionsDb: InspectionsDb by inject()
    private val projectsDb: ProjectsDb by inject()
    private val useCase: DeleteInspectionUseCase by inject()

    private val projectId = UuidProvider.getUuid()
    private val inspectionId = UuidProvider.getUuid()

    private val backendInspection =
        BackendInspection(
            inspection =
                Inspection(
                    id = inspectionId,
                    projectId = projectId,
                    startedAt = null,
                    endedAt = null,
                    participants = null,
                    climate = null,
                    note = null,
                    updatedAt = Timestamp(10L),
                ),
        )

    private suspend fun seedClosedProject() {
        projectsDb.saveProject(
            BackendProject(
                project =
                    Project(
                        id = projectId,
                        name = "Test Project",
                        address = "Test Address",
                        isClosed = true,
                        updatedAt = Timestamp(1L),
                    ),
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
}
