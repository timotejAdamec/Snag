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

import cz.adamec.timotej.snag.feat.findings.be.model.BackendFindingData
import cz.adamec.timotej.snag.feat.findings.business.model.FindingType
import cz.adamec.timotej.snag.feat.structures.be.model.BackendStructureData
import cz.adamec.timotej.snag.findings.be.app.api.DeleteFindingUseCase
import cz.adamec.timotej.snag.findings.be.app.api.model.DeleteFindingRequest
import cz.adamec.timotej.snag.findings.be.ports.FindingsDb
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.common.UuidProvider
import cz.adamec.timotej.snag.projects.be.model.BackendProjectData
import cz.adamec.timotej.snag.projects.be.ports.ProjectsDb
import cz.adamec.timotej.snag.structures.be.ports.StructuresDb
import cz.adamec.timotej.snag.testinfra.be.BackendKoinInitializedTest
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DeleteFindingUseCaseImplTest : BackendKoinInitializedTest() {
    private val findingsDb: FindingsDb by inject()
    private val projectsDb: ProjectsDb by inject()
    private val structuresDb: StructuresDb by inject()
    private val useCase: DeleteFindingUseCase by inject()

    private val projectId = UuidProvider.getUuid()
    private val structureId = UuidProvider.getUuid()
    private val findingId = UuidProvider.getUuid()

    private val backendFinding =
        BackendFindingData(
            id = findingId,
            structureId = structureId,
            name = "Crack in wall",
            description = null,
            type = FindingType.Classic(),
            coordinates = emptySet(),
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
        structuresDb.saveStructure(
            BackendStructureData(
                id = structureId,
                projectId = projectId,
                name = "Test Structure",
                floorPlanUrl = null,
                updatedAt = Timestamp(1L),
            ),
        )
    }

    @Test
    fun `returns existing entity when project is closed`() =
        runTest(testDispatcher) {
            seedClosedProject()
            findingsDb.saveFinding(backendFinding)

            val result =
                useCase(
                    DeleteFindingRequest(
                        findingId = findingId,
                        deletedAt = Timestamp(20L),
                    ),
                )

            assertEquals(backendFinding, result)
            val stored = findingsDb.getFinding(findingId)
            assertNull(stored?.deletedAt)
        }
}
