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

import cz.adamec.timotej.snag.feat.inspections.business.Inspection
import cz.adamec.timotej.snag.feat.inspections.fe.app.api.SaveInspectionUseCase
import cz.adamec.timotej.snag.feat.inspections.fe.app.api.model.SaveInspectionRequest
import cz.adamec.timotej.snag.feat.inspections.fe.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.feat.inspections.fe.model.FrontendInspection
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsDb
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsSync
import cz.adamec.timotej.snag.lib.core.common.TimestampProvider
import cz.adamec.timotej.snag.lib.core.common.UuidProvider
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.core.fe.log
import cz.adamec.timotej.snag.lib.core.fe.map
import kotlin.uuid.Uuid

class SaveInspectionUseCaseImpl(
    private val inspectionsDb: InspectionsDb,
    private val inspectionsSync: InspectionsSync,
    private val uuidProvider: UuidProvider,
    private val timestampProvider: TimestampProvider,
) : SaveInspectionUseCase {
    override suspend operator fun invoke(request: SaveInspectionRequest): OfflineFirstDataResult<Uuid> {
        val feInspection =
            FrontendInspection(
                inspection =
                    Inspection(
                        id = request.id ?: uuidProvider.getUuid(),
                        projectId = request.projectId,
                        startedAt = request.startedAt,
                        endedAt = request.endedAt,
                        participants = request.participants,
                        climate = request.climate,
                        note = request.note,
                        updatedAt = timestampProvider.getNowTimestamp(),
                    ),
            )

        val result = inspectionsDb.saveInspection(feInspection)
        logger.log(
            offlineFirstDataResult = result,
            additionalInfo = "SaveInspectionUseCase, inspectionsDb.saveInspection($feInspection)",
        )
        if (result is OfflineFirstDataResult.Success) {
            inspectionsSync.enqueueInspectionSave(feInspection.inspection.id)
        }
        return result.map {
            feInspection.inspection.id
        }
    }
}
