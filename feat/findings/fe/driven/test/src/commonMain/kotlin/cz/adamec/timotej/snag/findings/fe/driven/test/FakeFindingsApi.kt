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

import cz.adamec.timotej.snag.feat.findings.fe.model.FrontendFinding
import cz.adamec.timotej.snag.findings.fe.ports.FindingSyncResult
import cz.adamec.timotej.snag.findings.fe.ports.FindingsApi
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import kotlin.uuid.Uuid

class FakeFindingsApi : FindingsApi {
    private val findings = mutableMapOf<Uuid, FrontendFinding>()
    var forcedFailure: OnlineDataResult.Failure? = null
    var saveFindingResponseOverride: ((FrontendFinding) -> OnlineDataResult<FrontendFinding?>)? = null
    var modifiedSinceResults: List<FindingSyncResult> = emptyList()

    override suspend fun getFindings(structureId: Uuid): OnlineDataResult<List<FrontendFinding>> {
        val failure = forcedFailure
        if (failure != null) return failure
        return OnlineDataResult.Success(findings.values.filter { it.finding.structureId == structureId })
    }

    override suspend fun deleteFinding(
        id: Uuid,
        deletedAt: Timestamp,
    ): OnlineDataResult<Unit> {
        val failure = forcedFailure
        if (failure != null) return failure
        findings.remove(id)
        return OnlineDataResult.Success(Unit)
    }

    override suspend fun saveFinding(finding: FrontendFinding): OnlineDataResult<FrontendFinding?> {
        val failure = forcedFailure
        if (failure != null) return failure
        val override = saveFindingResponseOverride
        return if (override != null) {
            override(finding)
        } else {
            findings[finding.finding.id] = finding
            OnlineDataResult.Success(finding)
        }
    }

    fun setFinding(finding: FrontendFinding) {
        findings[finding.finding.id] = finding
    }

    override suspend fun getFindingsModifiedSince(
        structureId: Uuid,
        since: Timestamp,
    ): OnlineDataResult<List<FindingSyncResult>> {
        val failure = forcedFailure
        if (failure != null) return failure
        return OnlineDataResult.Success(modifiedSinceResults)
    }

    fun setFindings(findings: List<FrontendFinding>) {
        findings.forEach { this.findings[it.finding.id] = it }
    }
}
