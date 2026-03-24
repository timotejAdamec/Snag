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

import cz.adamec.timotej.snag.feat.inspections.be.app.api.SaveInspectionUseCase
import cz.adamec.timotej.snag.feat.inspections.be.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.feat.inspections.be.model.BackendInspection
import cz.adamec.timotej.snag.feat.inspections.be.ports.InspectionsDb
import cz.adamec.timotej.snag.projects.be.app.api.GetProjectUseCase
import cz.adamec.timotej.snag.projects.business.AreProjectEntitiesEditableRule

internal class SaveInspectionUseCaseImpl(
    private val inspectionsDb: InspectionsDb,
    private val getProjectUseCase: GetProjectUseCase,
    private val areProjectEntitiesEditableRule: AreProjectEntitiesEditableRule,
) : SaveInspectionUseCase {
    override suspend operator fun invoke(backendInspection: BackendInspection): BackendInspection? {
        val project = getProjectUseCase(backendInspection.projectId)
        if (project != null && !areProjectEntitiesEditableRule(project)) {
            return inspectionsDb.getInspection(backendInspection.id)
        }
        logger.debug("Saving inspection {} to local storage.", backendInspection)
        val isRejected = inspectionsDb.saveInspection(backendInspection)
        if (isRejected != null) {
            logger.debug(
                "Didn't save inspection {} to local storage as there is a newer one." +
                    " Returning the newer one ({}).",
                backendInspection,
                isRejected,
            )
        } else {
            logger.debug("Saved inspection {} to local storage.", backendInspection)
        }
        return isRejected
    }
}
