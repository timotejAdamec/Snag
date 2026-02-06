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

package cz.adamec.timotej.snag.findings.fe.ports

import cz.adamec.timotej.snag.feat.findings.fe.model.FrontendFinding
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import kotlin.uuid.Uuid

sealed interface FindingSyncResult {
    data class Deleted(val id: Uuid) : FindingSyncResult
    data class Updated(val finding: FrontendFinding) : FindingSyncResult
}

interface FindingsApi {
    suspend fun getFindings(structureId: Uuid): OnlineDataResult<List<FrontendFinding>>

    suspend fun saveFinding(finding: FrontendFinding): OnlineDataResult<FrontendFinding?>

    suspend fun deleteFinding(id: Uuid): OnlineDataResult<Unit>

    suspend fun getFindingsModifiedSince(structureId: Uuid, since: Timestamp): OnlineDataResult<List<FindingSyncResult>>
}
