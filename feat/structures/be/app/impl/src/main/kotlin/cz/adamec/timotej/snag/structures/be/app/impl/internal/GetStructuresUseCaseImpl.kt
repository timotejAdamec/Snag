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

import cz.adamec.timotej.snag.feat.structures.business.Structure
import cz.adamec.timotej.snag.structures.be.app.api.GetStructuresUseCase
import cz.adamec.timotej.snag.structures.be.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.structures.be.ports.StructuresLocalDataSource
import kotlin.uuid.Uuid

internal class GetStructuresUseCaseImpl(
    private val structuresLocalDataSource: StructuresLocalDataSource,
) : GetStructuresUseCase {
    override suspend operator fun invoke(projectId: Uuid): List<Structure> {
        logger.debug("Getting structures for project $projectId from local storage.")
        return structuresLocalDataSource.getStructures(projectId).also {
            logger.debug("Got ${it.size} structures for project $projectId from local storage.")
        }
    }
}
