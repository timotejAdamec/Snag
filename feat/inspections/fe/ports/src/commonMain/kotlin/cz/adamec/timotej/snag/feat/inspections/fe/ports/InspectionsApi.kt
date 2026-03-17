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

package cz.adamec.timotej.snag.feat.inspections.fe.ports

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.feat.inspections.fe.model.FrontendInspection
import kotlin.uuid.Uuid

sealed interface InspectionSyncResult {
    data class Deleted(
        val id: Uuid,
    ) : InspectionSyncResult

    data class Updated(
        val inspection: FrontendInspection,
    ) : InspectionSyncResult
}

interface InspectionsApi {
    suspend fun getInspections(projectId: Uuid): OnlineDataResult<List<FrontendInspection>>

    suspend fun saveInspection(frontendInspection: FrontendInspection): OnlineDataResult<FrontendInspection?>

    suspend fun deleteInspection(
        id: Uuid,
        deletedAt: Timestamp,
    ): OnlineDataResult<FrontendInspection?>

    suspend fun getInspectionsModifiedSince(
        projectId: Uuid,
        since: Timestamp,
    ): OnlineDataResult<List<InspectionSyncResult>>
}
