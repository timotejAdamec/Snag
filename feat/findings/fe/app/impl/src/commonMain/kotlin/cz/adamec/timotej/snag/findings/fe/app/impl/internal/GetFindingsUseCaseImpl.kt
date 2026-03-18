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

package cz.adamec.timotej.snag.findings.fe.app.impl.internal

import cz.adamec.timotej.snag.core.foundation.common.ApplicationScope
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.feat.findings.app.model.AppFinding
import cz.adamec.timotej.snag.findings.fe.app.api.GetFindingsUseCase
import cz.adamec.timotej.snag.findings.fe.app.impl.internal.sync.FINDING_SYNC_ENTITY_TYPE
import cz.adamec.timotej.snag.findings.fe.ports.FindingsDb
import cz.adamec.timotej.snag.sync.fe.app.api.ExecutePullSyncUseCase
import cz.adamec.timotej.snag.sync.fe.app.api.model.ExecutePullSyncRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

class GetFindingsUseCaseImpl(
    private val executePullSyncUseCase: ExecutePullSyncUseCase,
    private val findingsDb: FindingsDb,
    private val applicationScope: ApplicationScope,
) : GetFindingsUseCase {
    override operator fun invoke(structureId: Uuid): Flow<OfflineFirstDataResult<List<AppFinding>>> {
        applicationScope.launch {
            executePullSyncUseCase(
                ExecutePullSyncRequest(
                    entityTypeId = FINDING_SYNC_ENTITY_TYPE,
                    scopeId = structureId,
                ),
            )
        }
        return findingsDb
            .getFindingsFlow(structureId)
            .distinctUntilChanged()
    }
}
