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
import cz.adamec.timotej.snag.network.fe.test.FakeApiOps
import kotlin.uuid.Uuid

class FakeFindingsApi : FindingsApi {
    private val ops =
        FakeApiOps<FrontendFinding, FindingSyncResult>(getId = { it.finding.id })

    var forcedFailure
        get() = ops.forcedFailure
        set(value) {
            ops.forcedFailure = value
        }

    var saveFindingResponseOverride
        get() = ops.saveResponseOverride
        set(value) {
            ops.saveResponseOverride = value
        }

    var modifiedSinceResults
        get() = ops.modifiedSinceResults
        set(value) {
            ops.modifiedSinceResults = value
        }

    override suspend fun getFindings(structureId: Uuid): OnlineDataResult<List<FrontendFinding>> =
        ops.getAllItems { it.finding.structureId == structureId }

    override suspend fun saveFinding(finding: FrontendFinding): OnlineDataResult<FrontendFinding?> =
        ops.saveItem(finding)

    override suspend fun deleteFinding(
        id: Uuid,
        deletedAt: Timestamp,
    ): OnlineDataResult<Unit> = ops.deleteItemById(id)

    override suspend fun getFindingsModifiedSince(
        structureId: Uuid,
        since: Timestamp,
    ): OnlineDataResult<List<FindingSyncResult>> = ops.getModifiedSinceItems()

    fun setFinding(finding: FrontendFinding) = ops.setItem(finding)

    fun setFindings(findings: List<FrontendFinding>) = ops.setItems(findings)
}
