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
import cz.adamec.timotej.snag.findings.be.app.api.GetFindingsModifiedSinceUseCase
import cz.adamec.timotej.snag.findings.be.app.api.model.GetFindingsModifiedSinceRequest
import cz.adamec.timotej.snag.findings.be.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.findings.be.ports.FindingsDb

internal class GetFindingsModifiedSinceUseCaseImpl(
    private val findingsDb: FindingsDb,
) : GetFindingsModifiedSinceUseCase {
    override suspend operator fun invoke(request: GetFindingsModifiedSinceRequest): List<BackendFinding> {
        logger.debug(
            "Getting findings modified since {} for structure {} from local storage.",
            request.since,
            request.structureId,
        )
        return findingsDb.getFindingsModifiedSince(request.structureId, request.since).also {
            logger.debug(
                "Got {} findings modified since {} for structure {} from local storage.",
                it.size,
                request.since,
                request.structureId,
            )
        }
    }
}
