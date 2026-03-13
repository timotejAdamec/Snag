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
import cz.adamec.timotej.snag.projects.business.CanEditProjectEntitiesRule
import cz.adamec.timotej.snag.structures.be.app.api.SaveStructureUseCase
import cz.adamec.timotej.snag.structures.be.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.structures.be.ports.StructuresDb

internal class SaveStructureUseCaseImpl(
    private val structuresDb: StructuresDb,
    private val getProjectUseCase: GetProjectUseCase,
    private val canEditProjectEntitiesRule: CanEditProjectEntitiesRule,
) : SaveStructureUseCase {
    override suspend operator fun invoke(backendStructure: BackendStructure): BackendStructure? {
        val project = getProjectUseCase(backendStructure.structure.projectId)
        if (project != null && !canEditProjectEntitiesRule(project.project)) {
            return structuresDb.getStructure(backendStructure.structure.id)
        }
        logger.debug("Saving structure {} to local storage.", backendStructure)
        val isRejected = structuresDb.saveStructure(backendStructure)
        if (isRejected != null) {
            logger.debug(
                "Didn't save structure {} to local storage as there is a newer one." +
                    " Returning the newer one ({}).",
                backendStructure,
                isRejected,
            )
        } else {
            logger.debug("Saved structure {} to local storage.", backendStructure)
        }
        return isRejected
    }
}
