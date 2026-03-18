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

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.feat.findings.app.model.AppFinding
import kotlin.uuid.Uuid

sealed interface FindingSyncResult {
    data class Deleted(
        val id: Uuid,
    ) : FindingSyncResult

    data class Updated(
        val finding: AppFinding,
    ) : FindingSyncResult
}

interface FindingsApi {
    suspend fun getFindings(structureId: Uuid): OnlineDataResult<List<AppFinding>>

    suspend fun saveFinding(finding: AppFinding): OnlineDataResult<AppFinding?>

    suspend fun deleteFinding(
        id: Uuid,
        deletedAt: Timestamp,
    ): OnlineDataResult<AppFinding?>

    suspend fun getFindingsModifiedSince(
        structureId: Uuid,
        since: Timestamp,
    ): OnlineDataResult<List<FindingSyncResult>>
}
