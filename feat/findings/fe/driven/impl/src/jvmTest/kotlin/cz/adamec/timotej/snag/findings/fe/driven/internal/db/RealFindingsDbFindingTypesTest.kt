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

package cz.adamec.timotej.snag.findings.fe.driven.internal.db

import cz.adamec.timotej.snag.feat.findings.business.Finding
import cz.adamec.timotej.snag.feat.findings.business.FindingType
import cz.adamec.timotej.snag.feat.findings.business.Importance
import cz.adamec.timotej.snag.feat.findings.business.Term
import cz.adamec.timotej.snag.feat.findings.fe.model.FrontendFinding
import cz.adamec.timotej.snag.findings.fe.ports.FindingsDb
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstUpdateDataResult
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.uuid.Uuid

class RealFindingsDbFindingTypesTest : FrontendKoinInitializedTest() {
    private val findingsDb: FindingsDb by inject()

    // region Factory helpers

    private fun classicFinding(
        id: Uuid = FINDING_ID_1,
        importance: Importance = Importance.HIGH,
        term: Term = Term.T1,
        updatedAt: Long = 100L,
    ): FrontendFinding =
        FrontendFinding(
            finding =
                Finding(
                    id = id,
                    structureId = STRUCTURE_ID,
                    name = "Finding",
                    description = null,
                    type = FindingType.Classic(importance = importance, term = term),
                    coordinates = emptyList(),
                    updatedAt = Timestamp(updatedAt),
                ),
        )

    private fun unvisitedFinding(
        id: Uuid = FINDING_ID_1,
        updatedAt: Long = 100L,
    ): FrontendFinding =
        FrontendFinding(
            finding =
                Finding(
                    id = id,
                    structureId = STRUCTURE_ID,
                    name = "Finding",
                    description = null,
                    type = FindingType.Unvisited,
                    coordinates = emptyList(),
                    updatedAt = Timestamp(updatedAt),
                ),
        )

    private fun noteFinding(
        id: Uuid = FINDING_ID_1,
        updatedAt: Long = 100L,
    ): FrontendFinding =
        FrontendFinding(
            finding =
                Finding(
                    id = id,
                    structureId = STRUCTURE_ID,
                    name = "Finding",
                    description = null,
                    type = FindingType.Note,
                    coordinates = emptyList(),
                    updatedAt = Timestamp(updatedAt),
                ),
        )

    // endregion

    // region Read helper

    private suspend fun getFindings(structureId: Uuid = STRUCTURE_ID): List<FrontendFinding> {
        val result = findingsDb.getFindingsFlow(structureId).first()
        assertIs<OfflineFirstDataResult.Success<List<FrontendFinding>>>(result)
        return result.data
    }

    // endregion

    // region Group 1: Save & Retrieve Each Type

    @Test
    fun `save and retrieve Classic finding preserves importance and term`() =
        runTest(testDispatcher) {
            val finding = classicFinding(importance = Importance.HIGH, term = Term.T2)

            findingsDb.saveFinding(finding)

            val result = getFindings().single()
            assertEquals(FindingType.Classic(Importance.HIGH, Term.T2), result.finding.type)
        }

    @Test
    fun `save and retrieve Unvisited finding`() =
        runTest(testDispatcher) {
            findingsDb.saveFinding(unvisitedFinding())

            val result = getFindings().single()
            assertIs<FindingType.Unvisited>(result.finding.type)
        }

    @Test
    fun `save and retrieve Note finding`() =
        runTest(testDispatcher) {
            findingsDb.saveFinding(noteFinding())

            val result = getFindings().single()
            assertIs<FindingType.Note>(result.finding.type)
        }

    // endregion

    // region Group 2: Type Change via saveFinding — Classic → Non-Classic

    @Test
    fun `changing Classic to Unvisited removes classic details`() =
        runTest(testDispatcher) {
            findingsDb.saveFinding(classicFinding(importance = Importance.HIGH, term = Term.T1, updatedAt = 100L))

            findingsDb.saveFinding(unvisitedFinding(updatedAt = 200L))

            val result = getFindings().single()
            assertIs<FindingType.Unvisited>(result.finding.type)
        }

    @Test
    fun `changing Classic to Note removes classic details`() =
        runTest(testDispatcher) {
            findingsDb.saveFinding(classicFinding(updatedAt = 100L))

            findingsDb.saveFinding(noteFinding(updatedAt = 200L))

            val result = getFindings().single()
            assertIs<FindingType.Note>(result.finding.type)
        }

    // endregion

    // region Group 3: Type Change via saveFinding — Non-Classic → Classic

    @Test
    fun `changing Unvisited to Classic creates classic details`() =
        runTest(testDispatcher) {
            findingsDb.saveFinding(unvisitedFinding(updatedAt = 100L))

            findingsDb.saveFinding(classicFinding(importance = Importance.LOW, term = Term.T3, updatedAt = 200L))

            val result = getFindings().single()
            assertEquals(FindingType.Classic(Importance.LOW, Term.T3), result.finding.type)
        }

    @Test
    fun `changing Note to Classic creates classic details`() =
        runTest(testDispatcher) {
            findingsDb.saveFinding(noteFinding(updatedAt = 100L))

            findingsDb.saveFinding(classicFinding(importance = Importance.MEDIUM, term = Term.CON, updatedAt = 200L))

            val result = getFindings().single()
            assertEquals(FindingType.Classic(Importance.MEDIUM, Term.CON), result.finding.type)
        }

    // endregion

    // region Group 4: Update & Delete

    @Test
    fun `updating Classic importance and term via saveFinding modifies classic details`() =
        runTest(testDispatcher) {
            findingsDb.saveFinding(classicFinding(importance = Importance.HIGH, term = Term.T1, updatedAt = 100L))

            findingsDb.saveFinding(classicFinding(importance = Importance.MEDIUM, term = Term.T2, updatedAt = 200L))

            val result = getFindings().single()
            assertEquals(FindingType.Classic(Importance.MEDIUM, Term.T2), result.finding.type)
        }

    @Test
    fun `deleting Classic finding and re-saving as Unvisited works`() =
        runTest(testDispatcher) {
            findingsDb.saveFinding(classicFinding(updatedAt = 100L))

            findingsDb.deleteFinding(FINDING_ID_1)

            findingsDb.saveFinding(unvisitedFinding(updatedAt = 300L))

            val result = getFindings().single()
            assertIs<FindingType.Unvisited>(result.finding.type)
        }

    @Test
    fun `multiple findings of different types in same structure`() =
        runTest(testDispatcher) {
            findingsDb.saveFinding(classicFinding(id = FINDING_ID_1, importance = Importance.HIGH, term = Term.T1))
            findingsDb.saveFinding(unvisitedFinding(id = FINDING_ID_2))
            findingsDb.saveFinding(noteFinding(id = FINDING_ID_3))

            val results = getFindings()

            assertEquals(3, results.size)
            val byId = results.associateBy { it.finding.id }
            assertEquals(FindingType.Classic(Importance.HIGH, Term.T1), byId[FINDING_ID_1]!!.finding.type)
            assertIs<FindingType.Unvisited>(byId[FINDING_ID_2]!!.finding.type)
            assertIs<FindingType.Note>(byId[FINDING_ID_3]!!.finding.type)
        }

    // endregion

    // region Group 5: updateFindingDetails (FE-specific)

    @Test
    fun `updateFindingDetails changing Classic to Unvisited removes classic details`() =
        runTest(testDispatcher) {
            findingsDb.saveFinding(classicFinding(importance = Importance.HIGH, term = Term.T1, updatedAt = 100L))

            val updateResult =
                findingsDb.updateFindingDetails(
                    id = FINDING_ID_1,
                    name = "Finding",
                    description = null,
                    findingType = FindingType.Unvisited,
                    updatedAt = Timestamp(200L),
                )
            assertIs<OfflineFirstUpdateDataResult.Success>(updateResult)

            val result = getFindings().single()
            assertIs<FindingType.Unvisited>(result.finding.type)
        }

    @Test
    fun `updateFindingDetails changing Unvisited to Classic creates classic details`() =
        runTest(testDispatcher) {
            findingsDb.saveFinding(unvisitedFinding(updatedAt = 100L))

            val updateResult =
                findingsDb.updateFindingDetails(
                    id = FINDING_ID_1,
                    name = "Finding",
                    description = null,
                    findingType = FindingType.Classic(importance = Importance.HIGH, term = Term.T2),
                    updatedAt = Timestamp(200L),
                )
            assertIs<OfflineFirstUpdateDataResult.Success>(updateResult)

            val result = getFindings().single()
            assertEquals(FindingType.Classic(Importance.HIGH, Term.T2), result.finding.type)
        }

    @Test
    fun `updateFindingDetails updating Classic importance and term`() =
        runTest(testDispatcher) {
            findingsDb.saveFinding(classicFinding(importance = Importance.HIGH, term = Term.T1, updatedAt = 100L))

            val updateResult =
                findingsDb.updateFindingDetails(
                    id = FINDING_ID_1,
                    name = "Finding",
                    description = null,
                    findingType = FindingType.Classic(importance = Importance.MEDIUM, term = Term.T2),
                    updatedAt = Timestamp(200L),
                )
            assertIs<OfflineFirstUpdateDataResult.Success>(updateResult)

            val result = getFindings().single()
            assertEquals(FindingType.Classic(Importance.MEDIUM, Term.T2), result.finding.type)
        }

    // endregion

    companion object {
        private val STRUCTURE_ID = Uuid.parse("00000000-0000-0000-0001-000000000001")
        private val FINDING_ID_1 = Uuid.parse("00000000-0000-0000-0002-000000000001")
        private val FINDING_ID_2 = Uuid.parse("00000000-0000-0000-0002-000000000002")
        private val FINDING_ID_3 = Uuid.parse("00000000-0000-0000-0002-000000000003")
    }
}
