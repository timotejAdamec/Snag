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

import cz.adamec.timotej.snag.core.foundation.common.TimestampProvider
import cz.adamec.timotej.snag.core.foundation.common.UuidProvider
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.core.network.fe.log
import cz.adamec.timotej.snag.core.network.fe.map
import cz.adamec.timotej.snag.feat.findings.app.model.AppFindingData
import cz.adamec.timotej.snag.findings.fe.app.api.SaveNewFindingUseCase
import cz.adamec.timotej.snag.findings.fe.app.api.model.SaveNewFindingRequest
import cz.adamec.timotej.snag.findings.fe.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.findings.fe.app.impl.internal.sync.FINDING_SYNC_ENTITY_TYPE
import cz.adamec.timotej.snag.findings.fe.ports.FindingsDb
import cz.adamec.timotej.snag.sync.fe.app.api.EnqueueSyncSaveUseCase
import cz.adamec.timotej.snag.sync.fe.app.api.model.EnqueueSyncSaveRequest
import kotlin.uuid.Uuid

class SaveNewFindingUseCaseImpl(
    private val findingsDb: FindingsDb,
    private val enqueueSyncSaveUseCase: EnqueueSyncSaveUseCase,
    private val uuidProvider: UuidProvider,
    private val timestampProvider: TimestampProvider,
) : SaveNewFindingUseCase {
    override suspend operator fun invoke(request: SaveNewFindingRequest): OfflineFirstDataResult<Uuid> {
        val findingId = uuidProvider.getUuid()
        val frontendFinding =
            AppFindingData(
                id = findingId,
                structureId = request.structureId,
                name = request.name,
                description = request.description,
                type = request.findingType,
                coordinates = request.coordinates,
                updatedAt = timestampProvider.getNowTimestamp(),
            )

        return findingsDb
            .saveFinding(frontendFinding)
            .also {
                logger.log(
                    offlineFirstDataResult = it,
                    additionalInfo = "SaveNewFindingUseCase, findingsDb.saveFinding($frontendFinding)",
                )
                if (it is OfflineFirstDataResult.Success) {
                    enqueueSyncSaveUseCase(
                        EnqueueSyncSaveRequest(
                            entityTypeId = FINDING_SYNC_ENTITY_TYPE,
                            entityId = findingId,
                        ),
                    )
                }
            }.map {
                findingId
            }
    }
}
