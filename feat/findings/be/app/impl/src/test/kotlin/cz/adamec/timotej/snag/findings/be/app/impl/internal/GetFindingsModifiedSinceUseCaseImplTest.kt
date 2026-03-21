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
import cz.adamec.timotej.snag.feat.findings.be.model.BackendFindingData
import cz.adamec.timotej.snag.feat.findings.business.FindingType
import cz.adamec.timotej.snag.feat.structures.be.model.BackendStructureData
import cz.adamec.timotej.snag.findings.be.app.api.GetFindingsModifiedSinceUseCase
import cz.adamec.timotej.snag.findings.be.app.api.model.GetFindingsModifiedSinceRequest
import cz.adamec.timotej.snag.findings.be.ports.FindingsDb
import cz.adamec.timotej.snag.projects.be.model.BackendProjectData
import cz.adamec.timotej.snag.projects.be.ports.ProjectsDb
import cz.adamec.timotej.snag.structures.be.ports.StructuresDb
import cz.adamec.timotej.snag.testinfra.be.BackendKoinInitializedTest
import cz.adamec.timotej.snag.users.be.model.BackendUserData
import cz.adamec.timotej.snag.users.be.ports.UsersDb
import cz.adamec.timotej.snag.users.business.UserRole
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
    private val usersDb: UsersDb by inject()
    private val useCase: GetFindingsModifiedSinceUseCase by inject()

    private val projectId = Uuid.parse("00000000-0000-0000-0000-000000000001")
    private val structureId = Uuid.parse("00000000-0000-0000-0001-000000000001")
    private val otherStructureId = Uuid.parse("00000000-0000-0000-0001-000000000002")

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

    private suspend fun seedParentEntities() {
        seedTestUser()
        projectsDb.saveProject(
            BackendProjectData(
                id = projectId,
                name = "Test Project",
                address = "Test Address",
                creatorId = TEST_USER_ID,
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
        structuresDb.saveStructure(
            BackendStructureData(
                id = otherStructureId,
                projectId = projectId,
                name = "Other Structure",
                floorPlanUrl = null,
                updatedAt = Timestamp(1L),
            ),
        )
    }

    @Test
    fun `returns empty list when no findings exist`() =
        runTest(testDispatcher) {
            val result = useCase(GetFindingsModifiedSinceRequest(structureId = structureId, since = Timestamp(100L)))

            assertTrue(result.isEmpty())
        }

    @Test
    fun `returns findings with updatedAt after since`() =
        runTest(testDispatcher) {
            seedParentEntities()
            val finding =
                BackendFindingData(
                    id = Uuid.parse("00000000-0000-0000-0002-000000000001"),
                    structureId = structureId,
                    name = "Crack in wall",
                    description = null,
                    type = FindingType.Classic(),
                    coordinates = emptySet(),
                    updatedAt = Timestamp(200L),
                )
            dataSource.saveFinding(finding)

            val result = useCase(GetFindingsModifiedSinceRequest(structureId = structureId, since = Timestamp(100L)))

            assertEquals(listOf(finding), result)
        }

    @Test
    fun `excludes findings from different structure`() =
        runTest(testDispatcher) {
            seedParentEntities()
            val finding =
                BackendFindingData(
                    id = Uuid.parse("00000000-0000-0000-0002-000000000001"),
                    structureId = otherStructureId,
                    name = "Other finding",
                    description = null,
                    type = FindingType.Classic(),
                    coordinates = emptySet(),
                    updatedAt = Timestamp(200L),
                )
            dataSource.saveFinding(finding)

            val result = useCase(GetFindingsModifiedSinceRequest(structureId = structureId, since = Timestamp(100L)))

            assertTrue(result.isEmpty())
        }

    @Test
    fun `returns deleted findings when deletedAt is after since`() =
        runTest(testDispatcher) {
            seedParentEntities()
            val finding =
                BackendFindingData(
                    id = Uuid.parse("00000000-0000-0000-0002-000000000001"),
                    structureId = structureId,
                    name = "Crack in wall",
                    description = null,
                    type = FindingType.Classic(),
                    coordinates = emptySet(),
                    updatedAt = Timestamp(50L),
                    deletedAt = Timestamp(200L),
                )
            dataSource.saveFinding(finding)

            val result = useCase(GetFindingsModifiedSinceRequest(structureId = structureId, since = Timestamp(100L)))

            assertEquals(listOf(finding), result)
        }

    @Test
    fun `excludes unchanged findings`() =
        runTest(testDispatcher) {
            seedParentEntities()
            val finding =
                BackendFindingData(
                    id = Uuid.parse("00000000-0000-0000-0002-000000000001"),
                    structureId = structureId,
                    name = "Crack in wall",
                    description = null,
                    type = FindingType.Classic(),
                    coordinates = emptySet(),
                    updatedAt = Timestamp(50L),
                )
            dataSource.saveFinding(finding)

            val result = useCase(GetFindingsModifiedSinceRequest(structureId = structureId, since = Timestamp(100L)))

            assertTrue(result.isEmpty())
        }

    companion object {
        private val TEST_USER_ID = Uuid.parse("00000000-0000-0000-0000-000000000042")
    }
}
