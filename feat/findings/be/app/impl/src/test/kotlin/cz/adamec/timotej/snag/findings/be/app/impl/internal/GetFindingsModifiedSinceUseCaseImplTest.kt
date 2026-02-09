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

import cz.adamec.timotej.snag.feat.findings.be.model.BackendFinding
import cz.adamec.timotej.snag.feat.findings.business.Finding
import cz.adamec.timotej.snag.feat.findings.business.Importance
import cz.adamec.timotej.snag.feat.findings.business.Term
import cz.adamec.timotej.snag.feat.structures.be.model.BackendStructure
import cz.adamec.timotej.snag.feat.structures.business.Structure
import cz.adamec.timotej.snag.findings.be.app.api.GetFindingsModifiedSinceUseCase
import cz.adamec.timotej.snag.findings.be.ports.FindingsDb
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.projects.be.model.BackendProject
import cz.adamec.timotej.snag.projects.be.ports.ProjectsDb
import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.structures.be.ports.StructuresDb
import cz.adamec.timotej.snag.testinfra.be.BackendKoinInitializedTest
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class GetFindingsModifiedSinceUseCaseImplTest : BackendKoinInitializedTest() {
    private val dataSource: FindingsDb by inject()
    private val projectsDb: ProjectsDb by inject()
    private val structuresDb: StructuresDb by inject()
    private val useCase: GetFindingsModifiedSinceUseCase by inject()

    private val projectId = Uuid.parse("00000000-0000-0000-0000-000000000001")
    private val structureId = Uuid.parse("00000000-0000-0000-0001-000000000001")
    private val otherStructureId = Uuid.parse("00000000-0000-0000-0001-000000000002")

    private suspend fun seedParentEntities() {
        projectsDb.saveProject(
            BackendProject(
                project = Project(
                    id = projectId,
                    name = "Test Project",
                    address = "Test Address",
                    updatedAt = Timestamp(1L),
                ),
            ),
        )
        structuresDb.saveStructure(
            BackendStructure(
                structure = Structure(
                    id = structureId,
                    projectId = projectId,
                    name = "Test Structure",
                    floorPlanUrl = null,
                    updatedAt = Timestamp(1L),
                ),
            ),
        )
        structuresDb.saveStructure(
            BackendStructure(
                structure = Structure(
                    id = otherStructureId,
                    projectId = projectId,
                    name = "Other Structure",
                    floorPlanUrl = null,
                    updatedAt = Timestamp(1L),
                ),
            ),
        )
    }

    @Test
    fun `returns empty list when no findings exist`() =
        runTest(testDispatcher) {
            val result = useCase(structureId = structureId, since = Timestamp(100L))

            assertTrue(result.isEmpty())
        }

    @Test
    fun `returns findings with updatedAt after since`() =
        runTest(testDispatcher) {
            seedParentEntities()
            val finding =
                BackendFinding(
                    finding = Finding(
                        id = Uuid.parse("00000000-0000-0000-0002-000000000001"),
                        structureId = structureId,
                        name = "Crack in wall",
                        description = null,
                        importance = Importance.MEDIUM,
                        term = Term.T1,
                        coordinates = emptyList(),
                        updatedAt = Timestamp(200L),
                    ),
                )
            dataSource.saveFinding(finding)

            val result = useCase(structureId = structureId, since = Timestamp(100L))

            assertEquals(listOf(finding), result)
        }

    @Test
    fun `excludes findings from different structure`() =
        runTest(testDispatcher) {
            seedParentEntities()
            val finding =
                BackendFinding(
                    finding = Finding(
                        id = Uuid.parse("00000000-0000-0000-0002-000000000001"),
                        structureId = otherStructureId,
                        name = "Other finding",
                        description = null,
                        importance = Importance.MEDIUM,
                        term = Term.T1,
                        coordinates = emptyList(),
                        updatedAt = Timestamp(200L),
                    ),
                )
            dataSource.saveFinding(finding)

            val result = useCase(structureId = structureId, since = Timestamp(100L))

            assertTrue(result.isEmpty())
        }

    @Test
    fun `returns deleted findings when deletedAt is after since`() =
        runTest(testDispatcher) {
            seedParentEntities()
            val finding =
                BackendFinding(
                    finding = Finding(
                        id = Uuid.parse("00000000-0000-0000-0002-000000000001"),
                        structureId = structureId,
                        name = "Crack in wall",
                        description = null,
                        importance = Importance.MEDIUM,
                        term = Term.T1,
                        coordinates = emptyList(),
                        updatedAt = Timestamp(50L),
                    ),
                    deletedAt = Timestamp(200L),
                )
            dataSource.saveFinding(finding)

            val result = useCase(structureId = structureId, since = Timestamp(100L))

            assertEquals(listOf(finding), result)
        }

    @Test
    fun `excludes unchanged findings`() =
        runTest(testDispatcher) {
            seedParentEntities()
            val finding =
                BackendFinding(
                    finding = Finding(
                        id = Uuid.parse("00000000-0000-0000-0002-000000000001"),
                        structureId = structureId,
                        name = "Crack in wall",
                        description = null,
                        importance = Importance.MEDIUM,
                        term = Term.T1,
                        coordinates = emptyList(),
                        updatedAt = Timestamp(50L),
                    ),
                )
            dataSource.saveFinding(finding)

            val result = useCase(structureId = structureId, since = Timestamp(100L))

            assertTrue(result.isEmpty())
        }
}
