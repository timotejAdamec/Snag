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

import cz.adamec.timotej.snag.findings.be.app.api.DeleteFindingUseCase
import cz.adamec.timotej.snag.findings.be.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.findings.be.ports.FindingsLocalDataSource
import kotlin.uuid.Uuid

internal class DeleteFindingUseCaseImpl(
    private val findingsLocalDataSource: FindingsLocalDataSource,
) : DeleteFindingUseCase {
    override suspend operator fun invoke(findingId: Uuid) {
        logger.debug("Deleting finding {} from local storage.", findingId)
        findingsLocalDataSource.deleteFinding(findingId)
        logger.debug("Deleted finding {} from local storage.", findingId)
    }
}
