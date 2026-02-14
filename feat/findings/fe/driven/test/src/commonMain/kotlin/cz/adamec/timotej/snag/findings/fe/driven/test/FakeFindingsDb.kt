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
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstUpdateDataResult
import cz.adamec.timotej.snag.lib.database.fe.test.FakeDbOps
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.update
import kotlin.uuid.Uuid

class FakeFindingsDb : FindingsDb {
    private val ops = FakeDbOps<FrontendFinding>(getId = { it.finding.id })

    var forcedFailure
        get() = ops.forcedFailure
        set(value) {
            ops.forcedFailure = value
        }

    override fun getFindingsFlow(structureId: Uuid): Flow<OfflineFirstDataResult<List<FrontendFinding>>> =
        ops.allItemsFlow { it.finding.structureId == structureId }

    override fun getFindingFlow(id: Uuid): Flow<OfflineFirstDataResult<FrontendFinding?>> = ops.itemByIdFlow(id)

    override suspend fun saveFinding(finding: FrontendFinding): OfflineFirstDataResult<Unit> = ops.saveOneItem(finding)

    override suspend fun saveFindings(findings: List<FrontendFinding>): OfflineFirstDataResult<Unit> = ops.saveManyItems(findings)

    override suspend fun deleteFinding(id: Uuid): OfflineFirstDataResult<Unit> = ops.deleteItem(id)

    override suspend fun updateFindingDetails(
        id: Uuid,
        name: String,
        description: String?,
        findingType: FindingType,
        updatedAt: Timestamp,
    ): OfflineFirstUpdateDataResult {
        val failure = ops.forcedFailure
        if (failure != null) {
            return OfflineFirstUpdateDataResult.ProgrammerError(failure.throwable)
        }
        val existing = ops.items.value[id]
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
            ops.items.update { it + (id to updatedFinding) }
            OfflineFirstUpdateDataResult.Success
        }
    }

    override suspend fun updateFindingCoordinates(
        id: Uuid,
        coordinates: List<RelativeCoordinate>,
        updatedAt: Timestamp,
    ): OfflineFirstUpdateDataResult {
        val failure = ops.forcedFailure
        if (failure != null) {
            return OfflineFirstUpdateDataResult.ProgrammerError(failure.throwable)
        }
        val existing = ops.items.value[id]
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
            ops.items.update { it + (id to updatedFinding) }
            OfflineFirstUpdateDataResult.Success
        }
    }

    override suspend fun deleteFindingsByStructureId(structureId: Uuid): OfflineFirstDataResult<Unit> =
        ops.deleteItemsWhere { it.finding.structureId != structureId }

    fun setFinding(finding: FrontendFinding) = ops.setItem(finding)

    fun setFindings(findings: List<FrontendFinding>) = ops.setItems(findings)
}
