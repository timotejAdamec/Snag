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

package cz.adamec.timotej.snag.feat.inspections.fe.app.impl.internal.sync

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.foundation.common.TimestampProvider
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.feat.inspections.fe.app.impl.internal.LH
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionSyncResult
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsApi
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsDb
import cz.adamec.timotej.snag.sync.fe.app.api.GetLastPullSyncedAtTimestampUseCase
import cz.adamec.timotej.snag.sync.fe.app.api.SetLastPullSyncedAtTimestampUseCase
import cz.adamec.timotej.snag.sync.fe.app.api.handler.DbApiPullSyncHandler
import kotlin.uuid.Uuid

internal class InspectionPullSyncHandler(
    private val inspectionsApi: InspectionsApi,
    private val inspectionsDb: InspectionsDb,
    getLastPullSyncedAtTimestampUseCase: GetLastPullSyncedAtTimestampUseCase,
    setLastPullSyncedAtTimestampUseCase: SetLastPullSyncedAtTimestampUseCase,
    timestampProvider: TimestampProvider,
) : DbApiPullSyncHandler<InspectionSyncResult>(
        logger = LH.logger,
        timestampProvider = timestampProvider,
        getLastPullSyncedAtTimestampUseCase = getLastPullSyncedAtTimestampUseCase,
        setLastPullSyncedAtTimestampUseCase = setLastPullSyncedAtTimestampUseCase,
    ) {
    override val entityTypeId: String = INSPECTION_SYNC_ENTITY_TYPE
    override val entityName: String = "inspection"

    override suspend fun fetchChangesFromApi(
        scopeId: Uuid?,
        since: Timestamp,
    ): OnlineDataResult<List<InspectionSyncResult>> =
        inspectionsApi.getInspectionsModifiedSince(
            projectId = scopeId!!,
            since = since,
        )

    override suspend fun applyChange(change: InspectionSyncResult) {
        when (change) {
            is InspectionSyncResult.Deleted -> {
                LH.logger.d { "Processing deleted inspection ${change.id}." }
                inspectionsDb.deleteInspection(change.id)
            }
            is InspectionSyncResult.Updated -> {
                LH.logger.d { "Processing updated inspection ${change.inspection.id}." }
                inspectionsDb.saveInspection(change.inspection)
            }
        }
    }
}
