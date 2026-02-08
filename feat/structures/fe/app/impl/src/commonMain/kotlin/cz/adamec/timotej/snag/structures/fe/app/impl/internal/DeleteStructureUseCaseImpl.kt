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

import cz.adamec.timotej.snag.findings.fe.app.api.DeleteLocalFindingsByStructureIdUseCase
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.core.fe.log
import cz.adamec.timotej.snag.structures.fe.app.api.DeleteStructureUseCase
import cz.adamec.timotej.snag.structures.fe.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.structures.fe.ports.StructuresDb
import cz.adamec.timotej.snag.structures.fe.ports.StructuresSync
import kotlin.uuid.Uuid

class DeleteStructureUseCaseImpl(
    private val structuresDb: StructuresDb,
    private val structuresSync: StructuresSync,
    private val deleteLocalFindingsByStructureIdUseCase: DeleteLocalFindingsByStructureIdUseCase,
) : DeleteStructureUseCase {
    override suspend operator fun invoke(structureId: Uuid): OfflineFirstDataResult<Unit> {
        deleteLocalFindingsByStructureIdUseCase(structureId)
        return structuresDb
            .deleteStructure(structureId)
            .also {
                logger.log(
                    offlineFirstDataResult = it,
                    additionalInfo = "deleteStructure, structuresDb.deleteStructure($structureId)",
                )
                if (it is OfflineFirstDataResult.Success) {
                    structuresSync.enqueueStructureDelete(structureId)
                }
            }
    }
}
