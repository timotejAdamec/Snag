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

package cz.adamec.timotej.snag.structures.fe.app.impl.internal

import cz.adamec.timotej.snag.core.foundation.common.TimestampProvider
import cz.adamec.timotej.snag.core.foundation.common.UuidProvider
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.core.network.fe.log
import cz.adamec.timotej.snag.core.network.fe.map
import cz.adamec.timotej.snag.feat.structures.app.model.AppStructureData
import cz.adamec.timotej.snag.structures.fe.app.api.SaveStructureUseCase
import cz.adamec.timotej.snag.structures.fe.app.api.model.SaveStructureRequest
import cz.adamec.timotej.snag.structures.fe.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.structures.fe.app.impl.internal.sync.STRUCTURE_SYNC_ENTITY_TYPE
import cz.adamec.timotej.snag.structures.fe.ports.StructuresDb
import cz.adamec.timotej.snag.sync.fe.app.api.EnqueueSyncSaveUseCase
import kotlin.uuid.Uuid

class SaveStructureUseCaseImpl(
    private val structuresDb: StructuresDb,
    private val enqueueSyncSaveUseCase: EnqueueSyncSaveUseCase,
    private val uuidProvider: UuidProvider,
    private val timestampProvider: TimestampProvider,
) : SaveStructureUseCase {
    override suspend operator fun invoke(request: SaveStructureRequest): OfflineFirstDataResult<Uuid> {
        val feStructure =
            AppStructureData(
                id = request.id ?: uuidProvider.getUuid(),
                projectId = request.projectId,
                name = request.name,
                floorPlanUrl = request.floorPlanUrl,
                updatedAt = timestampProvider.getNowTimestamp(),
            )

        return structuresDb
            .saveStructure(feStructure)
            .also {
                logger.log(
                    offlineFirstDataResult = it,
                    additionalInfo = "SaveStructureUseCase, structuresDb.saveStructure($feStructure)",
                )
                if (it is OfflineFirstDataResult.Success) {
                    enqueueSyncSaveUseCase(
                        entityTypeId = STRUCTURE_SYNC_ENTITY_TYPE,
                        entityId = feStructure.id,
                    )
                }
            }.map {
                feStructure.id
            }
    }
}
