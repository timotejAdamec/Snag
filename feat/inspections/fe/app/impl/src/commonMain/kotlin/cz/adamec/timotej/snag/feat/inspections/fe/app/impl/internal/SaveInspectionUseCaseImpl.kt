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

package cz.adamec.timotej.snag.feat.inspections.fe.app.impl.internal

import cz.adamec.timotej.snag.core.foundation.common.TimestampProvider
import cz.adamec.timotej.snag.core.foundation.common.UuidProvider
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.core.network.fe.log
import cz.adamec.timotej.snag.core.network.fe.map
import cz.adamec.timotej.snag.feat.inspections.app.model.AppInspectionData
import cz.adamec.timotej.snag.feat.inspections.fe.app.api.SaveInspectionUseCase
import cz.adamec.timotej.snag.feat.inspections.fe.app.api.model.SaveInspectionRequest
import cz.adamec.timotej.snag.feat.inspections.fe.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.feat.inspections.fe.app.impl.internal.sync.INSPECTION_SYNC_ENTITY_TYPE
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsDb
import cz.adamec.timotej.snag.sync.fe.app.api.EnqueueSyncSaveUseCase
import kotlin.uuid.Uuid

class SaveInspectionUseCaseImpl(
    private val inspectionsDb: InspectionsDb,
    private val enqueueSyncSaveUseCase: EnqueueSyncSaveUseCase,
    private val uuidProvider: UuidProvider,
    private val timestampProvider: TimestampProvider,
) : SaveInspectionUseCase {
    override suspend operator fun invoke(request: SaveInspectionRequest): OfflineFirstDataResult<Uuid> {
        val feInspection =
            AppInspectionData(
                id = request.id ?: uuidProvider.getUuid(),
                projectId = request.projectId,
                startedAt = request.startedAt,
                endedAt = request.endedAt,
                participants = request.participants,
                climate = request.climate,
                note = request.note,
                updatedAt = timestampProvider.getNowTimestamp(),
            )

        val result = inspectionsDb.saveInspection(feInspection)
        logger.log(
            offlineFirstDataResult = result,
            additionalInfo = "SaveInspectionUseCase, inspectionsDb.saveInspection($feInspection)",
        )
        if (result is OfflineFirstDataResult.Success) {
            enqueueSyncSaveUseCase(
                entityTypeId = INSPECTION_SYNC_ENTITY_TYPE,
                entityId = feInspection.id,
            )
        }
        return result.map {
            feInspection.id
        }
    }
}
