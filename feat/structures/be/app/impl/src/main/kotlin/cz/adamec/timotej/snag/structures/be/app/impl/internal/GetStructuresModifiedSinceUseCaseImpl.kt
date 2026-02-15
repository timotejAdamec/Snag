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
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.structures.be.app.api.GetStructuresModifiedSinceUseCase
import cz.adamec.timotej.snag.structures.be.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.structures.be.ports.StructuresDb
import kotlin.uuid.Uuid

internal class GetStructuresModifiedSinceUseCaseImpl(
    private val structuresDb: StructuresDb,
) : GetStructuresModifiedSinceUseCase {
    override suspend operator fun invoke(
        projectId: Uuid,
        since: Timestamp,
    ): List<BackendStructure> {
        logger.debug("Getting structures modified since {} for project {} from local storage.", since, projectId)
        return structuresDb.getStructuresModifiedSince(projectId, since).also {
            logger.debug("Got {} structures modified since {} for project {} from local storage.", it.size, since, projectId)
        }
    }
}
