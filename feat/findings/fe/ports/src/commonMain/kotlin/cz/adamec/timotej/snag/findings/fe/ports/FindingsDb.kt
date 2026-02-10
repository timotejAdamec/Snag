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

import cz.adamec.timotej.snag.feat.findings.business.FindingType
import cz.adamec.timotej.snag.feat.findings.business.RelativeCoordinate
import cz.adamec.timotej.snag.feat.findings.fe.model.FrontendFinding
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstUpdateDataResult
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

interface FindingsDb {
    fun getFindingsFlow(structureId: Uuid): Flow<OfflineFirstDataResult<List<FrontendFinding>>>

    suspend fun saveFindings(findings: List<FrontendFinding>): OfflineFirstDataResult<Unit>

    suspend fun saveFinding(finding: FrontendFinding): OfflineFirstDataResult<Unit>

    fun getFindingFlow(id: Uuid): Flow<OfflineFirstDataResult<FrontendFinding?>>

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
        coordinates: List<RelativeCoordinate>,
        updatedAt: Timestamp,
    ): OfflineFirstUpdateDataResult

    suspend fun deleteFindingsByStructureId(structureId: Uuid): OfflineFirstDataResult<Unit>
}
