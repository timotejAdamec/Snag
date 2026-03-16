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

import cz.adamec.timotej.snag.findings.fe.app.api.SaveFindingCoordinatesUseCase
import cz.adamec.timotej.snag.findings.fe.app.api.model.SaveFindingCoordinatesRequest
import cz.adamec.timotej.snag.findings.fe.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.findings.fe.app.impl.internal.sync.FINDING_SYNC_ENTITY_TYPE
import cz.adamec.timotej.snag.findings.fe.ports.FindingsDb
import cz.adamec.timotej.snag.lib.core.common.TimestampProvider
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstUpdateDataResult
import cz.adamec.timotej.snag.lib.core.fe.log
import cz.adamec.timotej.snag.lib.sync.fe.app.api.EnqueueSyncSaveUseCase
import cz.adamec.timotej.snag.lib.sync.fe.app.api.model.EnqueueSyncSaveRequest

class SaveFindingCoordinatesUseCaseImpl(
    private val findingsDb: FindingsDb,
    private val enqueueSyncSaveUseCase: EnqueueSyncSaveUseCase,
    private val timestampProvider: TimestampProvider,
) : SaveFindingCoordinatesUseCase {
    override suspend operator fun invoke(request: SaveFindingCoordinatesRequest): OfflineFirstUpdateDataResult =
        findingsDb
            .updateFindingCoordinates(
                id = request.findingId,
                coordinates = request.coordinates,
                updatedAt = timestampProvider.getNowTimestamp(),
            ).also {
                logger.log(
                    offlineFirstUpdateDataResult = it,
                    additionalInfo = "SaveFindingCoordinatesUseCase, findingsDb.updateFindingCoordinates(${request.findingId})",
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
