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

import FrontendStructure
import cz.adamec.timotej.snag.feat.structures.business.Structure
import cz.adamec.timotej.snag.lib.core.common.TimestampProvider
import cz.adamec.timotej.snag.lib.core.common.UuidProvider
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.core.fe.log
import cz.adamec.timotej.snag.lib.core.fe.map
import cz.adamec.timotej.snag.structures.fe.app.api.SaveStructureUseCase
import cz.adamec.timotej.snag.structures.fe.app.api.model.SaveStructureRequest
import cz.adamec.timotej.snag.structures.fe.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.structures.fe.ports.StructuresDb
import cz.adamec.timotej.snag.structures.fe.ports.StructuresSync
import kotlin.uuid.Uuid

class SaveStructureUseCaseImpl(
    private val structuresDb: StructuresDb,
    private val structuresSync: StructuresSync,
    private val uuidProvider: UuidProvider,
    private val timestampProvider: TimestampProvider,
) : SaveStructureUseCase {
    override suspend operator fun invoke(request: SaveStructureRequest): OfflineFirstDataResult<Uuid> {
        val feStructure =
            FrontendStructure(
                structure = Structure(
                    id = request.id ?: uuidProvider.getUuid(),
                    projectId = request.projectId,
                    name = request.name,
                    floorPlanUrl = null,
                    updatedAt = timestampProvider.getNowTimestamp(),
                )
            )

        return structuresDb
            .saveStructure(feStructure)
            .also {
                logger.log(
                    offlineFirstDataResult = it,
                    additionalInfo = "SaveStructureUseCase, structuresDb.saveStructure($feStructure)",
                )
                if (it is OfflineFirstDataResult.Success) {
                    structuresSync.enqueueStructureSave(feStructure.structure.id)
                }
            }.map {
                feStructure.structure.id
            }
    }
}
