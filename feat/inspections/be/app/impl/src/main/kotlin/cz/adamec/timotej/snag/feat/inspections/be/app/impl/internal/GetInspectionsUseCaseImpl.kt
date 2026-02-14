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

import cz.adamec.timotej.snag.feat.inspections.be.app.api.GetInspectionsUseCase
import cz.adamec.timotej.snag.feat.inspections.be.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.feat.inspections.be.model.BackendInspection
import cz.adamec.timotej.snag.feat.inspections.be.ports.InspectionsDb
import kotlin.uuid.Uuid

internal class GetInspectionsUseCaseImpl(
    private val inspectionsDb: InspectionsDb,
) : GetInspectionsUseCase {
    override suspend operator fun invoke(projectId: Uuid): List<BackendInspection> {
        logger.debug("Getting inspections for project {} from local storage.", projectId)
        return inspectionsDb.getInspections(projectId).also {
            logger.debug("Got {} inspections for project {} from local storage.", it.size, projectId)
        }
    }
}
