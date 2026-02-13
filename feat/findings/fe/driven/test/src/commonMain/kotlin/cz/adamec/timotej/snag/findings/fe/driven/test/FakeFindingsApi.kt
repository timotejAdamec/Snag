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
import cz.adamec.timotej.snag.lib.core.fe.test.FakeEntityApi
import kotlin.uuid.Uuid

class FakeFindingsApi :
    FakeEntityApi<FrontendFinding, FindingSyncResult>(
        getId = { it.finding.id },
    ),
    FindingsApi {
    var saveFindingResponseOverride
        get() = saveResponseOverride
        set(value) {
            saveResponseOverride = value
        }

    override suspend fun getFindings(structureId: Uuid) = getAllItems { it.finding.structureId == structureId }

    override suspend fun saveFinding(finding: FrontendFinding) = saveItem(finding)

    override suspend fun deleteFinding(
        id: Uuid,
        deletedAt: Timestamp,
    ) = deleteItemById(id)

    override suspend fun getFindingsModifiedSince(
        structureId: Uuid,
        since: Timestamp,
    ) = getModifiedSinceItems()

    fun setFinding(finding: FrontendFinding) = setItem(finding)

    fun setFindings(findings: List<FrontendFinding>) = setItems(findings)
}
