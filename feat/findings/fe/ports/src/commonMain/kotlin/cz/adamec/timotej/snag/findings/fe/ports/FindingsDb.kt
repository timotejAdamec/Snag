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
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstUpdateDataResult
import cz.adamec.timotej.snag.feat.findings.app.model.AppFinding
import cz.adamec.timotej.snag.feat.findings.business.FindingType
import cz.adamec.timotej.snag.feat.findings.business.RelativeCoordinate
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

interface FindingsDb {
    fun getFindingsFlow(structureId: Uuid): Flow<OfflineFirstDataResult<List<AppFinding>>>

    suspend fun saveFindings(findings: List<AppFinding>): OfflineFirstDataResult<Unit>

    suspend fun saveFinding(finding: AppFinding): OfflineFirstDataResult<Unit>

    fun getFindingFlow(id: Uuid): Flow<OfflineFirstDataResult<AppFinding?>>

    suspend fun deleteFinding(id: Uuid): OfflineFirstDataResult<Unit>

    suspend fun updateFindingDetails(
        id: Uuid,
        name: String,
        description: String?,
        findingType: FindingType,
        updatedAt: Timestamp,
    ): OfflineFirstUpdateDataResult

    suspend fun updateFindingCoordinates(
        id: Uuid,
        coordinates: Set<RelativeCoordinate>,
        updatedAt: Timestamp,
    ): OfflineFirstUpdateDataResult

    suspend fun deleteFindingsByStructureId(structureId: Uuid): OfflineFirstDataResult<Unit>
}
