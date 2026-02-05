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

package cz.adamec.timotej.snag.findings.fe.driven.internal.sync

import cz.adamec.timotej.snag.feat.findings.fe.model.FrontendFinding
import cz.adamec.timotej.snag.findings.fe.driven.internal.LH
import cz.adamec.timotej.snag.findings.fe.ports.FindingsApi
import cz.adamec.timotej.snag.findings.fe.ports.FindingsDb
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.lib.sync.fe.app.api.handler.DbApiSyncHandler
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

internal class FindingSyncHandler(
    private val findingsApi: FindingsApi,
    private val findingsDb: FindingsDb,
) : DbApiSyncHandler<FrontendFinding>(LH.logger) {
    override val entityTypeId: String = FINDING_SYNC_ENTITY_TYPE
    override val entityName: String = "finding"

    override fun getEntityFlow(entityId: Uuid): Flow<OfflineFirstDataResult<FrontendFinding?>> =
        findingsDb.getFindingFlow(entityId)

    override suspend fun saveEntityToApi(entity: FrontendFinding): OnlineDataResult<FrontendFinding?> =
        findingsApi.saveFinding(entity)

    override suspend fun deleteEntityFromApi(entityId: Uuid): OnlineDataResult<Unit> =
        findingsApi.deleteFinding(entityId)

    override suspend fun saveEntityToDb(entity: FrontendFinding): OfflineFirstDataResult<Unit> =
        findingsDb.saveFinding(entity)
}
