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
import cz.adamec.timotej.snag.projects.be.driven.test.TEST_PROJECT_ID
import cz.adamec.timotej.snag.projects.be.driven.test.seedTestProject
import cz.adamec.timotej.snag.projects.be.ports.ProjectsDb
import cz.adamec.timotej.snag.structures.be.app.api.GetStructuresUseCase
import cz.adamec.timotej.snag.structures.be.ports.StructuresDb
import cz.adamec.timotej.snag.testinfra.be.BackendKoinInitializedTest
import cz.adamec.timotej.snag.users.be.driven.test.seedTestUser
import cz.adamec.timotej.snag.users.be.ports.UsersDb
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

class GetStructuresUseCaseImplTest : BackendKoinInitializedTest() {
    private val dataSource: StructuresDb by inject()
    private val projectsDb: ProjectsDb by inject()
    private val usersDb: UsersDb by inject()
    private val useCase: GetStructuresUseCase by inject()

    private val otherProjectId = Uuid.parse("00000000-0000-0000-0000-000000000002")

    private val structure1 =
        BackendStructureData(
            id = Uuid.parse("00000000-0000-0000-0001-000000000001"),
            projectId = TEST_PROJECT_ID,
            name = "Ground Floor",
            floorPlanUrl = null,
            updatedAt = Timestamp(1L),
        )
    private val structure2 =
        BackendStructureData(
            id = Uuid.parse("00000000-0000-0000-0001-000000000002"),
            projectId = TEST_PROJECT_ID,
            name = "First Floor",
            floorPlanUrl = "https://example.com/plan.jpg",
            updatedAt = Timestamp(2L),
        )
    private val otherStructure =
        BackendStructureData(
            id = Uuid.parse("00000000-0000-0000-0001-000000000003"),
            projectId = otherProjectId,
            name = "Other Building",
            floorPlanUrl = null,
            updatedAt = Timestamp(3L),
        )

    @Test
    fun `returns empty list when none`() =
        runTest(testDispatcher) {
            val result = useCase(TEST_PROJECT_ID)

            assertEquals(emptyList(), result)
        }

    @Test
    fun `returns structures for project`() =
        runTest(testDispatcher) {
            usersDb.seedTestUser()
            projectsDb.seedTestProject()
            projectsDb.seedTestProject(
                id = otherProjectId,
                name = "Other Project",
                address = "Other Address",
            )
            dataSource.saveStructure(structure1)
            dataSource.saveStructure(structure2)
            dataSource.saveStructure(otherStructure)

            val result = useCase(TEST_PROJECT_ID)

            assertEquals(listOf(structure1, structure2), result)
        }

    @Test
    fun `excludes other project structures`() =
        runTest(testDispatcher) {
            usersDb.seedTestUser()
            projectsDb.seedTestProject(
                id = otherProjectId,
                name = "Other Project",
                address = "Other Address",
            )
            dataSource.saveStructure(otherStructure)

            val result = useCase(TEST_PROJECT_ID)

            assertEquals(emptyList(), result)
        }
}
