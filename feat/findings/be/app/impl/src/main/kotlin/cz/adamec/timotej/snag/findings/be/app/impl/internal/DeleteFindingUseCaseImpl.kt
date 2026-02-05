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
import cz.adamec.timotej.snag.findings.be.ports.FindingsLocalDataSource

internal class DeleteFindingUseCaseImpl(
    private val findingsLocalDataSource: FindingsLocalDataSource,
) : DeleteFindingUseCase {
    override suspend operator fun invoke(request: DeleteFindingRequest): BackendFinding? {
        logger.debug("Deleting finding {} from local storage.", request.findingId)
        return findingsLocalDataSource.deleteFinding(
            id = request.findingId,
            deletedAt = request.deletedAt,
        ).also {
            logger.debug("Deleted finding {} from local storage.", request.findingId)
        }
    }
}
