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

package cz.adamec.timotej.snag.feat.inspections.be.app.impl.internal

import cz.adamec.timotej.snag.feat.inspections.be.app.api.DeleteInspectionUseCase
import cz.adamec.timotej.snag.feat.inspections.be.app.api.model.DeleteInspectionRequest
import cz.adamec.timotej.snag.feat.inspections.be.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.feat.inspections.be.model.BackendInspection
import cz.adamec.timotej.snag.feat.inspections.be.ports.InspectionsDb
import cz.adamec.timotej.snag.projects.be.app.api.GetProjectUseCase
import cz.adamec.timotej.snag.projects.business.CanEditProjectEntitiesRule

internal class DeleteInspectionUseCaseImpl(
    private val inspectionsDb: InspectionsDb,
    private val getProjectUseCase: GetProjectUseCase,
    private val canEditProjectEntitiesRule: CanEditProjectEntitiesRule,
) : DeleteInspectionUseCase {
    override suspend operator fun invoke(request: DeleteInspectionRequest): BackendInspection? {
        val inspection = inspectionsDb.getInspection(request.inspectionId)
        if (inspection != null) {
            val project = getProjectUseCase(inspection.inspection.projectId)
            if (project != null && !canEditProjectEntitiesRule(project.project)) {
                return inspection
            }
        }
        logger.debug("Deleting inspection {} from local storage.", request.inspectionId)
        val isRejected =
            inspectionsDb.deleteInspection(id = request.inspectionId, deletedAt = request.deletedAt)
        if (isRejected != null) {
            logger.debug(
                "Found newer version of inspection {} in local storage. Returning it instead.",
                request.inspectionId,
            )
        } else {
            logger.debug("Deleted inspection {} from local storage.", request.inspectionId)
        }
        return isRejected
    }
}
