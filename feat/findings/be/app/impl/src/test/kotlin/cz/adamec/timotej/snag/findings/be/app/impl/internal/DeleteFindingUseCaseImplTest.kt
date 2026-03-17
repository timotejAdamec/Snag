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
import cz.adamec.timotej.snag.feat.findings.be.model.BackendFinding
import cz.adamec.timotej.snag.feat.findings.business.Finding
import cz.adamec.timotej.snag.feat.findings.business.FindingType
import cz.adamec.timotej.snag.feat.structures.be.model.BackendStructure
import cz.adamec.timotej.snag.feat.structures.business.Structure
import cz.adamec.timotej.snag.findings.be.app.api.DeleteFindingUseCase
import cz.adamec.timotej.snag.findings.be.app.api.model.DeleteFindingRequest
import cz.adamec.timotej.snag.findings.be.ports.FindingsDb
import cz.adamec.timotej.snag.projects.be.model.BackendProject
import cz.adamec.timotej.snag.projects.be.ports.ProjectsDb
import cz.adamec.timotej.snag.projects.business.Project
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
        BackendFinding(
            finding =
                Finding(
                    id = findingId,
                    structureId = structureId,
                    name = "Crack in wall",
                    description = null,
                    type = FindingType.Classic(),
                    coordinates = emptySet(),
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
        structuresDb.saveStructure(
            BackendStructure(
                structure =
                    Structure(
                        id = structureId,
                        projectId = projectId,
                        name = "Test Structure",
                        floorPlanUrl = null,
                        updatedAt = Timestamp(1L),
                    ),
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
