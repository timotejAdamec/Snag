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

import cz.adamec.timotej.snag.feat.findings.business.Coordinate
import cz.adamec.timotej.snag.feat.findings.business.Finding
import cz.adamec.timotej.snag.findings.fe.ports.FindingsDb
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstUpdateDataResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlin.uuid.Uuid

class FakeFindingsDb : FindingsDb {
    private val findings = MutableStateFlow<Map<Uuid, Finding>>(emptyMap())
    var forcedFailure: OfflineFirstDataResult.ProgrammerError? = null

    override fun getFindingsFlow(structureId: Uuid): Flow<OfflineFirstDataResult<List<Finding>>> =
        findings.map { map ->
            val failure = forcedFailure
            failure ?: OfflineFirstDataResult.Success(map.values.filter { it.structureId == structureId })
        }

    override suspend fun saveFindings(findings: List<Finding>): OfflineFirstDataResult<Unit> {
        val failure = forcedFailure
        if (failure != null) return failure

        this.findings.update { current ->
            current + findings.associateBy { it.id }
        }
        return OfflineFirstDataResult.Success(Unit)
    }

    override suspend fun saveFinding(finding: Finding): OfflineFirstDataResult<Unit> {
        val failure = forcedFailure
        if (failure != null) return failure

        findings.update { it + (finding.id to finding) }
        return OfflineFirstDataResult.Success(Unit)
    }

    override suspend fun deleteFinding(id: Uuid): OfflineFirstDataResult<Unit> {
        val failure = forcedFailure
        if (failure != null) return failure

        findings.update { it - id }
        return OfflineFirstDataResult.Success(Unit)
    }

    override fun getFindingFlow(id: Uuid): Flow<OfflineFirstDataResult<Finding?>> =
        findings.map { map ->
            val failure = forcedFailure
            if (failure != null) {
                failure
            } else {
                OfflineFirstDataResult.Success(map[id])
            }
        }

    fun setFinding(finding: Finding) {
        findings.update { it + (finding.id to finding) }
    }

    override suspend fun updateFindingDetails(
        id: Uuid,
        name: String,
        description: String?,
    ): OfflineFirstUpdateDataResult {
        val failure = forcedFailure
        if (failure != null) return OfflineFirstUpdateDataResult.ProgrammerError(failure.throwable)
        val existing = findings.value[id] ?: return OfflineFirstUpdateDataResult.NotFound
        findings.update { it + (id to existing.copy(name = name, description = description)) }
        return OfflineFirstUpdateDataResult.Success
    }

    override suspend fun updateFindingCoordinates(
        id: Uuid,
        coordinates: List<Coordinate>,
    ): OfflineFirstUpdateDataResult {
        val failure = forcedFailure
        if (failure != null) return OfflineFirstUpdateDataResult.ProgrammerError(failure.throwable)
        val existing = findings.value[id] ?: return OfflineFirstUpdateDataResult.NotFound
        findings.update { it + (id to existing.copy(coordinates = coordinates)) }
        return OfflineFirstUpdateDataResult.Success
    }

    fun setFindings(findings: List<Finding>) {
        this.findings.update { current ->
            current + findings.associateBy { it.id }
        }
    }
}
