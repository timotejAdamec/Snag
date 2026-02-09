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

import cz.adamec.timotej.snag.feat.findings.business.Importance
import cz.adamec.timotej.snag.feat.findings.business.RelativeCoordinate
import cz.adamec.timotej.snag.feat.findings.business.Term
import cz.adamec.timotej.snag.feat.findings.fe.model.FrontendFinding
import cz.adamec.timotej.snag.findings.fe.ports.FindingsDb
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstUpdateDataResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlin.uuid.Uuid

class FakeFindingsDb : FindingsDb {
    private val findings = MutableStateFlow<Map<Uuid, FrontendFinding>>(emptyMap())
    var forcedFailure: OfflineFirstDataResult.ProgrammerError? = null

    override fun getFindingsFlow(structureId: Uuid): Flow<OfflineFirstDataResult<List<FrontendFinding>>> =
        findings.map { map ->
            val failure = forcedFailure
            failure ?: OfflineFirstDataResult.Success(map.values.filter { it.finding.structureId == structureId })
        }

    override suspend fun saveFindings(findings: List<FrontendFinding>): OfflineFirstDataResult<Unit> {
        val failure = forcedFailure
        if (failure != null) return failure

        this.findings.update { current ->
            current + findings.associateBy { it.finding.id }
        }
        return OfflineFirstDataResult.Success(Unit)
    }

    override suspend fun saveFinding(finding: FrontendFinding): OfflineFirstDataResult<Unit> {
        val failure = forcedFailure
        if (failure != null) return failure

        findings.update { it + (finding.finding.id to finding) }
        return OfflineFirstDataResult.Success(Unit)
    }

    override suspend fun deleteFinding(id: Uuid): OfflineFirstDataResult<Unit> {
        val failure = forcedFailure
        if (failure != null) return failure

        findings.update { it - id }
        return OfflineFirstDataResult.Success(Unit)
    }

    override fun getFindingFlow(id: Uuid): Flow<OfflineFirstDataResult<FrontendFinding?>> =
        findings.map { map ->
            val failure = forcedFailure
            if (failure != null) {
                failure
            } else {
                OfflineFirstDataResult.Success(map[id])
            }
        }

    fun setFinding(finding: FrontendFinding) {
        findings.update { it + (finding.finding.id to finding) }
    }

    override suspend fun updateFindingDetails(
        id: Uuid,
        name: String,
        description: String?,
        importance: Importance,
        term: Term,
        updatedAt: Timestamp,
    ): OfflineFirstUpdateDataResult {
        val failure = forcedFailure
        if (failure != null) {
            return OfflineFirstUpdateDataResult.ProgrammerError(failure.throwable)
        }
        val existing = findings.value[id]
        return if (existing == null) {
            OfflineFirstUpdateDataResult.NotFound
        } else {
            val updatedFinding =
                FrontendFinding(
                    finding =
                        existing.finding.copy(
                            name = name,
                            description = description,
                            importance = importance,
                            term = term,
                            updatedAt = updatedAt,
                        ),
                )
            findings.update { it + (id to updatedFinding) }
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
        val existing = findings.value[id]
        return if (existing == null) {
            OfflineFirstUpdateDataResult.NotFound
        } else {
            val updatedFinding = FrontendFinding(finding = existing.finding.copy(coordinates = coordinates, updatedAt = updatedAt))
            findings.update { it + (id to updatedFinding) }
            OfflineFirstUpdateDataResult.Success
        }
    }

    override suspend fun deleteFindingsByStructureId(structureId: Uuid): OfflineFirstDataResult<Unit> {
        val failure = forcedFailure
        if (failure != null) return failure
        findings.update { current -> current.filterValues { it.finding.structureId != structureId } }
        return OfflineFirstDataResult.Success(Unit)
    }

    fun setFindings(findings: List<FrontendFinding>) {
        this.findings.update { current ->
            current + findings.associateBy { it.finding.id }
        }
    }
}
