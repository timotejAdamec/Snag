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

package cz.adamec.timotej.snag.findings.fe.driven.test

import cz.adamec.timotej.snag.feat.findings.business.FindingType
import cz.adamec.timotej.snag.feat.findings.business.RelativeCoordinate
import cz.adamec.timotej.snag.feat.findings.fe.model.FrontendFinding
import cz.adamec.timotej.snag.findings.fe.ports.FindingsDb
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstUpdateDataResult
import cz.adamec.timotej.snag.lib.core.fe.test.FakeEntityDb
import kotlinx.coroutines.flow.update
import kotlin.uuid.Uuid

class FakeFindingsDb :
    FakeEntityDb<FrontendFinding>(
        getId = { it.finding.id },
    ),
    FindingsDb {
    override fun getFindingsFlow(structureId: Uuid) = allItemsFlow { it.finding.structureId == structureId }

    override fun getFindingFlow(id: Uuid) = itemByIdFlow(id)

    override suspend fun saveFinding(finding: FrontendFinding) = saveOneItem(finding)

    override suspend fun saveFindings(findings: List<FrontendFinding>) = saveManyItems(findings)

    override suspend fun deleteFinding(id: Uuid) = deleteItem(id)

    override suspend fun deleteFindingsByStructureId(structureId: Uuid) = deleteItemsWhere { it.finding.structureId != structureId }

    override suspend fun updateFindingDetails(
        id: Uuid,
        name: String,
        description: String?,
        findingType: FindingType,
        updatedAt: Timestamp,
    ): OfflineFirstUpdateDataResult {
        val failure = forcedFailure
        if (failure != null) {
            return OfflineFirstUpdateDataResult.ProgrammerError(failure.throwable)
        }
        val existing = items.value[id]
        return if (existing == null) {
            OfflineFirstUpdateDataResult.NotFound
        } else {
            val updatedFinding =
                FrontendFinding(
                    finding =
                        existing.finding.copy(
                            name = name,
                            description = description,
                            type = findingType,
                            updatedAt = updatedAt,
                        ),
                )
            items.update { it + (id to updatedFinding) }
            OfflineFirstUpdateDataResult.Success
        }
    }

    override suspend fun updateFindingCoordinates(
        id: Uuid,
        coordinates: List<RelativeCoordinate>,
        updatedAt: Timestamp,
    ): OfflineFirstUpdateDataResult {
        val failure = forcedFailure
        if (failure != null) {
            return OfflineFirstUpdateDataResult.ProgrammerError(failure.throwable)
        }
        val existing = items.value[id]
        return if (existing == null) {
            OfflineFirstUpdateDataResult.NotFound
        } else {
            val updatedFinding =
                FrontendFinding(
                    finding =
                        existing.finding.copy(
                            coordinates = coordinates,
                            updatedAt = updatedAt,
                        ),
                )
            items.update { it + (id to updatedFinding) }
            OfflineFirstUpdateDataResult.Success
        }
    }

    fun setFinding(finding: FrontendFinding) = setItem(finding)

    fun setFindings(findings: List<FrontendFinding>) = setItems(findings)
}
