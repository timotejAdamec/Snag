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

package cz.adamec.timotej.snag.findings.be.app.impl.internal

import cz.adamec.timotej.snag.feat.findings.be.model.BackendFinding
import cz.adamec.timotej.snag.findings.be.app.api.SaveFindingUseCase
import cz.adamec.timotej.snag.findings.be.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.findings.be.ports.FindingsDb
import cz.adamec.timotej.snag.projects.be.app.api.GetProjectUseCase
import cz.adamec.timotej.snag.projects.business.CanEditProjectEntitiesRule
import cz.adamec.timotej.snag.structures.be.ports.StructuresDb

internal class SaveFindingUseCaseImpl(
    private val findingsDb: FindingsDb,
    private val structuresDb: StructuresDb,
    private val getProjectUseCase: GetProjectUseCase,
    private val canEditProjectEntitiesRule: CanEditProjectEntitiesRule,
) : SaveFindingUseCase {
    override suspend operator fun invoke(finding: BackendFinding): BackendFinding? {
        val structure = structuresDb.getStructure(finding.finding.structureId)
        if (structure != null) {
            val project = getProjectUseCase(structure.structure.projectId)
            if (project != null && !canEditProjectEntitiesRule(project.project)) {
                return findingsDb.getFinding(finding.finding.id)
            }
        }
        logger.debug("Saving finding {} to local storage.", finding)
        val isRejected = findingsDb.saveFinding(finding)
        if (isRejected != null) {
            logger.debug(
                "Didn't save finding {} to local storage as there is a newer one." +
                    " Returning the newer one ({}).",
                finding,
                isRejected,
            )
        } else {
            logger.debug("Saved finding {} to local storage.", finding)
        }
        return isRejected
    }
}
