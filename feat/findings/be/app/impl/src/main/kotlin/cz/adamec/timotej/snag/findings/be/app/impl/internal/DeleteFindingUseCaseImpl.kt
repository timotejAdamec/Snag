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
import cz.adamec.timotej.snag.lib.sync.be.DeleteConflictResult
import cz.adamec.timotej.snag.lib.sync.be.resolveConflictForDelete

internal class DeleteFindingUseCaseImpl(
    private val findingsDb: FindingsDb,
) : DeleteFindingUseCase {
    override suspend operator fun invoke(request: DeleteFindingRequest): BackendFinding? {
        logger.debug("Deleting finding {} from local storage.", request.findingId)
        val existing = findingsDb.getFinding(request.findingId)
        return when (val result = resolveConflictForDelete(existing, request.deletedAt)) {
            is DeleteConflictResult.Proceed -> {
                findingsDb.softDeleteFinding(id = request.findingId, deletedAt = request.deletedAt)
                logger.debug("Deleted finding {} from local storage.", request.findingId)
                null
            }
            is DeleteConflictResult.NotFound -> {
                logger.debug("Finding {} not found in local storage.", request.findingId)
                null
            }
            is DeleteConflictResult.AlreadyDeleted -> {
                logger.debug("Finding {} already deleted in local storage.", request.findingId)
                null
            }
            is DeleteConflictResult.Rejected -> {
                logger.debug(
                    "Found newer version of finding {} in local storage. Returning it instead.",
                    request.findingId,
                )
                result.serverVersion
            }
        }
    }
}
