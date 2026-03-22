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

package cz.adamec.timotej.snag.findings.be.driven.impl.internal

import cz.adamec.timotej.snag.authorization.business.UserRole
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.feat.findings.be.model.BackendFinding
import cz.adamec.timotej.snag.feat.findings.be.model.BackendFindingData
import cz.adamec.timotej.snag.feat.findings.business.FindingType
import cz.adamec.timotej.snag.feat.findings.business.Importance
import cz.adamec.timotej.snag.feat.findings.business.RelativeCoordinate
import cz.adamec.timotej.snag.feat.findings.business.Term
import cz.adamec.timotej.snag.feat.structures.be.model.BackendStructureData
import cz.adamec.timotej.snag.findings.be.ports.FindingsDb
import cz.adamec.timotej.snag.projects.be.model.BackendProjectData
import cz.adamec.timotej.snag.projects.be.ports.ProjectsDb
import cz.adamec.timotej.snag.structures.be.ports.StructuresDb
import cz.adamec.timotej.snag.testinfra.be.BackendKoinInitializedTest
import cz.adamec.timotej.snag.users.be.model.BackendUserData
import cz.adamec.timotej.snag.users.be.ports.UsersDb
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.uuid.Uuid

class RealFindingsDbFindingTypesTest : BackendKoinInitializedTest() {
    private val findingsDb: FindingsDb by inject()
    private val projectsDb: ProjectsDb by inject()
    private val structuresDb: StructuresDb by inject()
    private val usersDb: UsersDb by inject()

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
                id = PROJECT_ID,
                name = "Test Project",
                address = "Test Address",
                creatorId = TEST_USER_ID,
                updatedAt = Timestamp(1L),
            ),
        )
        structuresDb.saveStructure(
            BackendStructureData(
                id = STRUCTURE_ID,
                projectId = PROJECT_ID,
                name = "Test Structure",
                floorPlanUrl = null,
                updatedAt = Timestamp(1L),
            ),
        )
    }

    private fun classicFinding(
        id: Uuid = FINDING_ID_1,
        importance: Importance = Importance.HIGH,
        term: Term = Term.T1,
        updatedAt: Long = 100L,
    ): BackendFinding =
        BackendFindingData(
            id = id,
            structureId = STRUCTURE_ID,
            name = "Finding",
            description = null,
            type = FindingType.Classic(importance = importance, term = term),
            coordinates = emptySet(),
            updatedAt = Timestamp(updatedAt),
        )

    private fun unvisitedFinding(
        id: Uuid = FINDING_ID_1,
        updatedAt: Long = 100L,
    ): BackendFinding =
        BackendFindingData(
            id = id,
            structureId = STRUCTURE_ID,
            name = "Finding",
            description = null,
            type = FindingType.Unvisited,
            coordinates = emptySet(),
            updatedAt = Timestamp(updatedAt),
        )

    private fun noteFinding(
        id: Uuid = FINDING_ID_1,
        updatedAt: Long = 100L,
    ): BackendFinding =
        BackendFindingData(
            id = id,
            structureId = STRUCTURE_ID,
            name = "Finding",
            description = null,
            type = FindingType.Note,
            coordinates = emptySet(),
            updatedAt = Timestamp(updatedAt),
        )

    // Group 1: Save & Retrieve Each Type

    @Test
    fun `save and retrieve Classic finding preserves importance and term`() =
        runTest(testDispatcher) {
            seedParentEntities()
            val finding = classicFinding(importance = Importance.HIGH, term = Term.T2)

            findingsDb.saveFinding(finding)

            val result = findingsDb.getFindings(STRUCTURE_ID).single()
            assertEquals(FindingType.Classic(Importance.HIGH, Term.T2), result.type)
        }

    @Test
    fun `save and retrieve Unvisited finding`() =
        runTest(testDispatcher) {
            seedParentEntities()
            val finding = unvisitedFinding()

            findingsDb.saveFinding(finding)

            val result = findingsDb.getFindings(STRUCTURE_ID).single()
            assertIs<FindingType.Unvisited>(result.type)
        }

    @Test
    fun `save and retrieve Note finding`() =
        runTest(testDispatcher) {
            seedParentEntities()
            val finding = noteFinding()

            findingsDb.saveFinding(finding)

            val result = findingsDb.getFindings(STRUCTURE_ID).single()
            assertIs<FindingType.Note>(result.type)
        }

    // Group 2: Type Change — Classic → Non-Classic

    @Test
    fun `changing Classic to Unvisited removes classic details`() =
        runTest(testDispatcher) {
            seedParentEntities()
            findingsDb.saveFinding(classicFinding(importance = Importance.HIGH, term = Term.T1, updatedAt = 100L))

            findingsDb.saveFinding(unvisitedFinding(updatedAt = 200L))

            val result = findingsDb.getFindings(STRUCTURE_ID).single()
            assertIs<FindingType.Unvisited>(result.type)
        }

    @Test
    fun `changing Classic to Note removes classic details`() =
        runTest(testDispatcher) {
            seedParentEntities()
            findingsDb.saveFinding(classicFinding(updatedAt = 100L))

            findingsDb.saveFinding(noteFinding(updatedAt = 200L))

            val result = findingsDb.getFindings(STRUCTURE_ID).single()
            assertIs<FindingType.Note>(result.type)
        }

    // Group 3: Type Change — Non-Classic → Classic

    @Test
    fun `changing Unvisited to Classic creates classic details`() =
        runTest(testDispatcher) {
            seedParentEntities()
            findingsDb.saveFinding(unvisitedFinding(updatedAt = 100L))

            findingsDb.saveFinding(classicFinding(importance = Importance.LOW, term = Term.T3, updatedAt = 200L))

            val result = findingsDb.getFindings(STRUCTURE_ID).single()
            assertEquals(FindingType.Classic(Importance.LOW, Term.T3), result.type)
        }

    @Test
    fun `changing Note to Classic creates classic details`() =
        runTest(testDispatcher) {
            seedParentEntities()
            findingsDb.saveFinding(noteFinding(updatedAt = 100L))

            findingsDb.saveFinding(classicFinding(importance = Importance.MEDIUM, term = Term.CON, updatedAt = 200L))

            val result = findingsDb.getFindings(STRUCTURE_ID).single()
            assertEquals(FindingType.Classic(Importance.MEDIUM, Term.CON), result.type)
        }

    // Group 4: Update & Delete

    @Test
    fun `updating Classic importance and term modifies existing classic details`() =
        runTest(testDispatcher) {
            seedParentEntities()
            findingsDb.saveFinding(classicFinding(importance = Importance.HIGH, term = Term.T1, updatedAt = 100L))

            findingsDb.saveFinding(classicFinding(importance = Importance.MEDIUM, term = Term.T2, updatedAt = 200L))

            val result = findingsDb.getFindings(STRUCTURE_ID).single()
            assertEquals(FindingType.Classic(Importance.MEDIUM, Term.T2), result.type)
        }

    @Test
    fun `soft-deleting Classic finding and re-saving as Unvisited works`() =
        runTest(testDispatcher) {
            seedParentEntities()
            findingsDb.saveFinding(classicFinding(updatedAt = 100L))

            findingsDb.deleteFinding(FINDING_ID_1, Timestamp(200L))

            findingsDb.saveFinding(unvisitedFinding(updatedAt = 300L))

            val result = findingsDb.getFindings(STRUCTURE_ID).single()
            assertIs<FindingType.Unvisited>(result.type)
        }

    @Test
    fun `multiple findings of different types in same structure`() =
        runTest(testDispatcher) {
            seedParentEntities()
            findingsDb.saveFinding(classicFinding(id = FINDING_ID_1, importance = Importance.HIGH, term = Term.T1))
            findingsDb.saveFinding(unvisitedFinding(id = FINDING_ID_2))
            findingsDb.saveFinding(noteFinding(id = FINDING_ID_3))

            val results = findingsDb.getFindings(STRUCTURE_ID)

            assertEquals(3, results.size)
            val byId = results.associateBy { it.id }
            assertEquals(FindingType.Classic(Importance.HIGH, Term.T1), byId[FINDING_ID_1]!!.type)
            assertIs<FindingType.Unvisited>(byId[FINDING_ID_2]!!.type)
            assertIs<FindingType.Note>(byId[FINDING_ID_3]!!.type)
        }

    // Group 5: Save Overwrites Type

    @Test
    fun `save overwrites Classic with Unvisited when newer`() =
        runTest(testDispatcher) {
            seedParentEntities()
            findingsDb.saveFinding(classicFinding(importance = Importance.HIGH, term = Term.T1, updatedAt = 100L))

            findingsDb.saveFinding(unvisitedFinding(updatedAt = 200L))

            val result = findingsDb.getFindings(STRUCTURE_ID).single()
            assertIs<FindingType.Unvisited>(result.type)
        }

    @Test
    fun `save overwrites Unvisited with Classic when newer`() =
        runTest(testDispatcher) {
            seedParentEntities()
            findingsDb.saveFinding(unvisitedFinding(updatedAt = 100L))

            findingsDb.saveFinding(classicFinding(updatedAt = 200L))

            val result = findingsDb.getFindings(STRUCTURE_ID).single()
            assertIs<FindingType.Classic>(result.type)
        }

    @Test
    fun `getFindingsModifiedSince returns findings with correct types`() =
        runTest(testDispatcher) {
            seedParentEntities()
            findingsDb.saveFinding(classicFinding(id = FINDING_ID_1, importance = Importance.HIGH, term = Term.T1, updatedAt = 200L))
            findingsDb.saveFinding(noteFinding(id = FINDING_ID_2, updatedAt = 300L))

            val results = findingsDb.getFindingsModifiedSince(STRUCTURE_ID, since = Timestamp(150L))

            assertEquals(2, results.size)
            val byId = results.associateBy { it.id }
            assertEquals(FindingType.Classic(Importance.HIGH, Term.T1), byId[FINDING_ID_1]!!.type)
            assertIs<FindingType.Note>(byId[FINDING_ID_2]!!.type)
        }

    @Test
    fun `save finding with coordinates and retrieve preserves coordinates`() =
        runTest(testDispatcher) {
            seedParentEntities()
            val coordinates =
                setOf(
                    RelativeCoordinate(0.1f, 0.2f),
                    RelativeCoordinate(0.3f, 0.4f),
                )
            val finding =
                BackendFindingData(
                    id = FINDING_ID_1,
                    structureId = STRUCTURE_ID,
                    name = "Finding",
                    description = null,
                    type = FindingType.Classic(),
                    coordinates = coordinates,
                    updatedAt = Timestamp(100L),
                )

            findingsDb.saveFinding(finding)

            val result = findingsDb.getFindings(STRUCTURE_ID).single()
            assertEquals(coordinates, result.coordinates)
        }

    companion object {
        private val TEST_USER_ID = Uuid.parse("00000000-0000-0000-0000-000000000042")
        private val PROJECT_ID = Uuid.parse("00000000-0000-0000-0000-000000000001")
        private val STRUCTURE_ID = Uuid.parse("00000000-0000-0000-0001-000000000001")
        private val FINDING_ID_1 = Uuid.parse("00000000-0000-0000-0002-000000000001")
        private val FINDING_ID_2 = Uuid.parse("00000000-0000-0000-0002-000000000002")
        private val FINDING_ID_3 = Uuid.parse("00000000-0000-0000-0002-000000000003")
    }
}
