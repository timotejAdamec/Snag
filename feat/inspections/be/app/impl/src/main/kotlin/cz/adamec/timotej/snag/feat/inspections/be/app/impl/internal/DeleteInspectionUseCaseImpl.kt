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
import cz.adamec.timotej.snag.lib.sync.be.DeleteConflictResult
import cz.adamec.timotej.snag.lib.sync.be.resolveConflictForDelete

internal class DeleteInspectionUseCaseImpl(
    private val inspectionsDb: InspectionsDb,
) : DeleteInspectionUseCase {
    override suspend operator fun invoke(request: DeleteInspectionRequest): BackendInspection? {
        logger.debug("Deleting inspection {} from local storage.", request.inspectionId)
        val existing = inspectionsDb.getInspection(request.inspectionId)
        return when (val result = resolveConflictForDelete(existing, request.deletedAt)) {
            is DeleteConflictResult.Proceed -> {
                inspectionsDb.softDeleteInspection(id = request.inspectionId, deletedAt = request.deletedAt)
                logger.debug("Deleted inspection {} from local storage.", request.inspectionId)
                null
            }
            is DeleteConflictResult.NotFound -> {
                logger.debug("Inspection {} not found in local storage.", request.inspectionId)
                null
            }
            is DeleteConflictResult.AlreadyDeleted -> {
                logger.debug("Inspection {} already deleted in local storage.", request.inspectionId)
                null
            }
            is DeleteConflictResult.Rejected -> {
                logger.debug(
                    "Found newer version of inspection {} in local storage. Returning it instead.",
                    request.inspectionId,
                )
                result.serverVersion
            }
        }
    }
}
