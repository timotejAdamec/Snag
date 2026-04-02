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
import cz.adamec.timotej.snag.findings.be.app.api.DeleteFindingUseCase
import cz.adamec.timotej.snag.findings.be.app.api.model.DeleteFindingRequest
import cz.adamec.timotej.snag.findings.be.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.findings.be.ports.FindingsDb
import cz.adamec.timotej.snag.projects.be.app.api.GetProjectUseCase
import cz.adamec.timotej.snag.projects.business.AreProjectEntitiesEditableRule
import cz.adamec.timotej.snag.structures.be.app.api.GetStructureUseCase

internal class DeleteFindingUseCaseImpl(
    private val findingsDb: FindingsDb,
    private val getStructureUseCase: GetStructureUseCase,
    private val getProjectUseCase: GetProjectUseCase,
    private val areProjectEntitiesEditableRule: AreProjectEntitiesEditableRule,
) : DeleteFindingUseCase {
    override suspend operator fun invoke(request: DeleteFindingRequest): BackendFinding? {
        val finding = findingsDb.getFinding(request.findingId)
        if (finding != null) {
            val structure = getStructureUseCase(finding.structureId)
            if (structure != null) {
                val project = getProjectUseCase(structure.projectId)
                if (project != null && !areProjectEntitiesEditableRule(project)) {
                    return finding
                }
            }
        }
        logger.debug("Deleting finding {} from local storage.", request.findingId)
        val isRejected =
            findingsDb.deleteFinding(id = request.findingId, deletedAt = request.deletedAt)
        if (isRejected != null) {
            logger.debug(
                "Found newer version of finding {} in local storage. Returning it instead.",
                request.findingId,
            )
        } else {
            logger.debug("Deleted finding {} from local storage.", request.findingId)
        }
        return isRejected
    }
}
