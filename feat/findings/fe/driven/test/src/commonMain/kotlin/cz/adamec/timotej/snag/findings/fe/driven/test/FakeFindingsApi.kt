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

import cz.adamec.timotej.snag.feat.findings.business.Finding
import cz.adamec.timotej.snag.findings.fe.ports.FindingsApi
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import kotlin.uuid.Uuid

class FakeFindingsApi : FindingsApi {
    private val findings = mutableMapOf<Uuid, Finding>()
    var forcedFailure: OnlineDataResult.Failure? = null
    var saveFindingResponseOverride: ((Finding) -> OnlineDataResult<Finding?>)? = null

    override suspend fun getFindings(structureId: Uuid): OnlineDataResult<List<Finding>> {
        val failure = forcedFailure
        if (failure != null) return failure
        return OnlineDataResult.Success(findings.values.filter { it.structureId == structureId })
    }

    override suspend fun deleteFinding(id: Uuid): OnlineDataResult<Unit> {
        val failure = forcedFailure
        if (failure != null) return failure
        findings.remove(id)
        return OnlineDataResult.Success(Unit)
    }

    override suspend fun saveFinding(finding: Finding): OnlineDataResult<Finding?> {
        val failure = forcedFailure
        if (failure != null) return failure
        val override = saveFindingResponseOverride
        if (override != null) return override(finding)
        findings[finding.id] = finding
        return OnlineDataResult.Success(finding)
    }

    fun setFinding(finding: Finding) {
        findings[finding.id] = finding
    }

    fun setFindings(findings: List<Finding>) {
        findings.forEach { this.findings[it.id] = it }
    }
}
