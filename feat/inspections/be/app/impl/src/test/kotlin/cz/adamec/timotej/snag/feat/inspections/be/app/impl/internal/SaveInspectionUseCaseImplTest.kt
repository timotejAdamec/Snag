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

import cz.adamec.timotej.snag.feat.inspections.be.app.api.SaveInspectionUseCase
import cz.adamec.timotej.snag.feat.inspections.be.model.BackendInspectionData
import cz.adamec.timotej.snag.feat.inspections.be.ports.InspectionsDb
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.common.UuidProvider
import cz.adamec.timotej.snag.projects.be.model.BackendProjectData
import cz.adamec.timotej.snag.projects.be.ports.ProjectsDb
import cz.adamec.timotej.snag.testinfra.be.BackendKoinInitializedTest
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SaveInspectionUseCaseImplTest : BackendKoinInitializedTest() {
    private val inspectionsDb: InspectionsDb by inject()
    private val projectsDb: ProjectsDb by inject()
    private val useCase: SaveInspectionUseCase by inject()

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

    private suspend fun seedClosedProject() {
        projectsDb.saveProject(
            BackendProjectData(
                id = projectId,
                name = "Test Project",
                address = "Test Address",
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

            val newInspection =
                backendInspection.copy(
                    participants = "Bob",
                    updatedAt = Timestamp(20L),
                )

            val result = useCase(newInspection)

            assertEquals(backendInspection, result)
            assertEquals(backendInspection, inspectionsDb.getInspection(inspectionId))
        }

    @Test
    fun `returns null when project is closed and entity not in DB`() =
        runTest(testDispatcher) {
            seedClosedProject()

            val result = useCase(backendInspection)

            assertNull(result)
        }
}
