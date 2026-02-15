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

package cz.adamec.timotej.snag.structures.be.app.impl.internal

import cz.adamec.timotej.snag.feat.structures.be.model.BackendStructure
import cz.adamec.timotej.snag.structures.be.app.api.DeleteStructureUseCase
import cz.adamec.timotej.snag.structures.be.app.api.model.DeleteStructureRequest
import cz.adamec.timotej.snag.structures.be.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.structures.be.ports.StructuresDb

internal class DeleteStructureUseCaseImpl(
    private val structuresDb: StructuresDb,
) : DeleteStructureUseCase {
    override suspend operator fun invoke(request: DeleteStructureRequest): BackendStructure? {
        logger.debug("Deleting structure {} from local storage.", request.structureId)
        return structuresDb
            .deleteStructure(
                id = request.structureId,
                deletedAt = request.deletedAt,
            ).also { newerStructure ->
                newerStructure?.let {
                    logger.debug(
                        "Found newer version of structure {} in local storage. Returning it instead.",
                        request.structureId,
                    )
                } ?: logger.debug("Deleted structure {} from local storage.", request.structureId)
            }
    }
}
