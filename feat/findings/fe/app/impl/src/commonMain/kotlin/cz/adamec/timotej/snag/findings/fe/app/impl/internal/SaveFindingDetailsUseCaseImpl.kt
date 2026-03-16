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

import cz.adamec.timotej.snag.findings.fe.app.api.SaveFindingDetailsUseCase
import cz.adamec.timotej.snag.findings.fe.app.api.model.SaveFindingDetailsRequest
import cz.adamec.timotej.snag.findings.fe.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.findings.fe.app.impl.internal.sync.FINDING_SYNC_ENTITY_TYPE
import cz.adamec.timotej.snag.findings.fe.ports.FindingsDb
import cz.adamec.timotej.snag.lib.core.common.TimestampProvider
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstUpdateDataResult
import cz.adamec.timotej.snag.lib.core.fe.log
import cz.adamec.timotej.snag.lib.sync.fe.app.api.EnqueueSyncSaveUseCase
import cz.adamec.timotej.snag.lib.sync.fe.app.api.model.EnqueueSyncSaveRequest

class SaveFindingDetailsUseCaseImpl(
    private val findingsDb: FindingsDb,
    private val enqueueSyncSaveUseCase: EnqueueSyncSaveUseCase,
    private val timestampProvider: TimestampProvider,
) : SaveFindingDetailsUseCase {
    override suspend operator fun invoke(request: SaveFindingDetailsRequest): OfflineFirstUpdateDataResult =
        findingsDb
            .updateFindingDetails(
                id = request.findingId,
                name = request.name,
                description = request.description,
                findingType = request.findingType,
                updatedAt = timestampProvider.getNowTimestamp(),
            ).also {
                logger.log(
                    offlineFirstUpdateDataResult = it,
                    additionalInfo = "SaveFindingDetailsUseCase, findingsDb.updateFindingDetails(${request.findingId})",
                )
                if (it is OfflineFirstUpdateDataResult.Success) {
                    enqueueSyncSaveUseCase(
                        EnqueueSyncSaveRequest(
                            entityTypeId = FINDING_SYNC_ENTITY_TYPE,
                            entityId = request.findingId,
                        ),
                    )
                }
            }
}
