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

internal class DeleteInspectionUseCaseImpl(
    private val inspectionsDb: InspectionsDb,
) : DeleteInspectionUseCase {
    override suspend operator fun invoke(request: DeleteInspectionRequest): BackendInspection? {
        logger.debug("Deleting inspection {} from local storage.", request.inspectionId)
        val result =
            inspectionsDb.deleteInspection(
                id = request.inspectionId,
                deletedAt = request.deletedAt,
            )
        result?.let {
            logger.debug(
                "Found newer version of inspection {} in local storage. Returning it instead.",
                request.inspectionId,
            )
        } ?: logger.debug("Deleted inspection {} from local storage.", request.inspectionId)
        return result
    }
}
