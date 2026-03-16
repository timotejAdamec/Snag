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
import cz.adamec.timotej.snag.projects.be.app.api.GetProjectUseCase
import cz.adamec.timotej.snag.projects.business.model.CanEditProjectEntitiesRule
import cz.adamec.timotej.snag.structures.be.app.api.DeleteStructureUseCase
import cz.adamec.timotej.snag.structures.be.app.api.model.DeleteStructureRequest
import cz.adamec.timotej.snag.structures.be.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.structures.be.ports.StructuresDb

internal class DeleteStructureUseCaseImpl(
    private val structuresDb: StructuresDb,
    private val getProjectUseCase: GetProjectUseCase,
    private val canEditProjectEntitiesRule: CanEditProjectEntitiesRule,
) : DeleteStructureUseCase {
    override suspend operator fun invoke(request: DeleteStructureRequest): BackendStructure? {
        val structure = structuresDb.getStructure(request.structureId)
        if (structure != null) {
            val project = getProjectUseCase(structure.projectId)
            if (project != null && !canEditProjectEntitiesRule(project)) {
                return structure
            }
        }
        logger.debug("Deleting structure {} from local storage.", request.structureId)
        val isRejected =
            structuresDb.deleteStructure(id = request.structureId, deletedAt = request.deletedAt)
        if (isRejected != null) {
            logger.debug(
                "Found newer version of structure {} in local storage. Returning it instead.",
                request.structureId,
            )
        } else {
            logger.debug("Deleted structure {} from local storage.", request.structureId)
        }
        return isRejected
    }
}
