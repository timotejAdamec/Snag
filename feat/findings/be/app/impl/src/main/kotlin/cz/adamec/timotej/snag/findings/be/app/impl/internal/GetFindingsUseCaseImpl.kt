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

import cz.adamec.timotej.snag.feat.findings.business.Finding
import cz.adamec.timotej.snag.findings.be.app.api.GetFindingsUseCase
import cz.adamec.timotej.snag.findings.be.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.findings.be.ports.FindingsLocalDataSource
import kotlin.uuid.Uuid

internal class GetFindingsUseCaseImpl(
    private val findingsLocalDataSource: FindingsLocalDataSource,
) : GetFindingsUseCase {
    override suspend operator fun invoke(structureId: Uuid): List<Finding> {
        logger.debug("Getting findings for structure $structureId from local storage.")
        return findingsLocalDataSource.getFindings(structureId).also {
            logger.debug("Got ${it.size} findings for structure $structureId from local storage.")
        }
    }
}
