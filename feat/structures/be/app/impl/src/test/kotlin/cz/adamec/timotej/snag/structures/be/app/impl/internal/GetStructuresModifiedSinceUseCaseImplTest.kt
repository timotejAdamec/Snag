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

package cz.adamec.timotej.snag.structures.be.app.impl.internal

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.feat.structures.be.model.BackendStructureData
import cz.adamec.timotej.snag.projects.be.model.BackendProjectData
import cz.adamec.timotej.snag.projects.be.ports.ProjectsDb
import cz.adamec.timotej.snag.structures.be.app.api.GetStructuresModifiedSinceUseCase
import cz.adamec.timotej.snag.structures.be.app.api.model.GetStructuresModifiedSinceRequest
import cz.adamec.timotej.snag.structures.be.ports.StructuresDb
import cz.adamec.timotej.snag.testinfra.be.BackendKoinInitializedTest
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class GetStructuresModifiedSinceUseCaseImplTest : BackendKoinInitializedTest() {
    private val dataSource: StructuresDb by inject()
    private val projectsDb: ProjectsDb by inject()
    private val useCase: GetStructuresModifiedSinceUseCase by inject()

    private val projectId = Uuid.parse("00000000-0000-0000-0000-000000000001")
    private val otherProjectId = Uuid.parse("00000000-0000-0000-0000-000000000002")

    @Test
    fun `returns empty list when no structures exist`() =
        runTest(testDispatcher) {
            val result = useCase(GetStructuresModifiedSinceRequest(projectId = projectId, since = Timestamp(100L)))

            assertTrue(result.isEmpty())
        }

    @Test
    fun `returns structures with updatedAt after since`() =
        runTest(testDispatcher) {
            projectsDb.saveProject(
                BackendProjectData(
                    id = projectId,
                    name = "Test Project",
                    address = "Test Address",
                    updatedAt = Timestamp(1L),
                ),
            )
            val structure =
                BackendStructureData(
                    id = Uuid.parse("00000000-0000-0000-0001-000000000001"),
                    projectId = projectId,
                    name = "Ground Floor",
                    floorPlanUrl = null,
                    updatedAt = Timestamp(200L),
                )
            dataSource.saveStructure(structure)

            val result = useCase(GetStructuresModifiedSinceRequest(projectId = projectId, since = Timestamp(100L)))

            assertEquals(listOf(structure), result)
        }

    @Test
    fun `excludes structures from different project`() =
        runTest(testDispatcher) {
            projectsDb.saveProject(
                BackendProjectData(
                    id = otherProjectId,
                    name = "Other Project",
                    address = "Other Address",
                    updatedAt = Timestamp(1L),
                ),
            )
            val structure =
                BackendStructureData(
                    id = Uuid.parse("00000000-0000-0000-0001-000000000001"),
                    projectId = otherProjectId,
                    name = "Other Building",
                    floorPlanUrl = null,
                    updatedAt = Timestamp(200L),
                )
            dataSource.saveStructure(structure)

            val result = useCase(GetStructuresModifiedSinceRequest(projectId = projectId, since = Timestamp(100L)))

            assertTrue(result.isEmpty())
        }

    @Test
    fun `returns deleted structures when deletedAt is after since`() =
        runTest(testDispatcher) {
            projectsDb.saveProject(
                BackendProjectData(
                    id = projectId,
                    name = "Test Project",
                    address = "Test Address",
                    updatedAt = Timestamp(1L),
                ),
            )
            val structure =
                BackendStructureData(
                    id = Uuid.parse("00000000-0000-0000-0001-000000000001"),
                    projectId = projectId,
                    name = "Ground Floor",
                    floorPlanUrl = null,
                    updatedAt = Timestamp(50L),
                    deletedAt = Timestamp(200L),
                )
            dataSource.saveStructure(structure)

            val result = useCase(GetStructuresModifiedSinceRequest(projectId = projectId, since = Timestamp(100L)))

            assertEquals(listOf(structure), result)
        }

    @Test
    fun `excludes unchanged structures`() =
        runTest(testDispatcher) {
            projectsDb.saveProject(
                BackendProjectData(
                    id = projectId,
                    name = "Test Project",
                    address = "Test Address",
                    updatedAt = Timestamp(1L),
                ),
            )
            val structure =
                BackendStructureData(
                    id = Uuid.parse("00000000-0000-0000-0001-000000000001"),
                    projectId = projectId,
                    name = "Ground Floor",
                    floorPlanUrl = null,
                    updatedAt = Timestamp(50L),
                )
            dataSource.saveStructure(structure)

            val result = useCase(GetStructuresModifiedSinceRequest(projectId = projectId, since = Timestamp(100L)))

            assertTrue(result.isEmpty())
        }
}
