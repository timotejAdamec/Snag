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
import cz.adamec.timotej.snag.feat.findings.app.model.AppFindingPhoto
import cz.adamec.timotej.snag.findings.fe.app.api.GetFindingPhotosUseCase
import cz.adamec.timotej.snag.findings.fe.app.impl.internal.sync.FINDING_PHOTO_SYNC_ENTITY_TYPE
import cz.adamec.timotej.snag.findings.fe.ports.FindingPhotosDb
import cz.adamec.timotej.snag.sync.fe.app.api.ExecutePullSyncUseCase
import cz.adamec.timotej.snag.sync.fe.app.api.model.ExecutePullSyncRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

internal class GetFindingPhotosUseCaseImpl(
    private val executePullSyncUseCase: ExecutePullSyncUseCase,
    private val findingPhotosDb: FindingPhotosDb,
    private val applicationScope: ApplicationScope,
) : GetFindingPhotosUseCase {
    override operator fun invoke(findingId: Uuid): Flow<OfflineFirstDataResult<List<AppFindingPhoto>>> {
        applicationScope.launch {
            executePullSyncUseCase(
                ExecutePullSyncRequest(
                    entityTypeId = FINDING_PHOTO_SYNC_ENTITY_TYPE,
                    scopeId = findingId,
                ),
            )
        }
        return findingPhotosDb
            .getPhotosFlow(findingId)
            .distinctUntilChanged()
    }
}
