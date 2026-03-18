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

package cz.adamec.timotej.snag.structures.be.app.impl.internal

import cz.adamec.timotej.snag.feat.structures.be.model.BackendStructure
import cz.adamec.timotej.snag.structures.be.app.api.GetStructuresModifiedSinceUseCase
import cz.adamec.timotej.snag.structures.be.app.api.model.GetStructuresModifiedSinceRequest
import cz.adamec.timotej.snag.structures.be.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.structures.be.ports.StructuresDb

internal class GetStructuresModifiedSinceUseCaseImpl(
    private val structuresDb: StructuresDb,
) : GetStructuresModifiedSinceUseCase {
    override suspend operator fun invoke(request: GetStructuresModifiedSinceRequest): List<BackendStructure> {
        logger.debug(
            "Getting structures modified since {} for project {} from local storage.",
            request.since,
            request.projectId,
        )
        return structuresDb.getStructuresModifiedSince(request.projectId, request.since).also {
            logger.debug(
                "Got {} structures modified since {} for project {} from local storage.",
                it.size,
                request.since,
                request.projectId,
            )
        }
    }
}
