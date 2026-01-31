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

import cz.adamec.timotej.snag.structures.be.app.api.DeleteStructureUseCase
import cz.adamec.timotej.snag.structures.be.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.structures.be.ports.StructuresLocalDataSource
import kotlin.uuid.Uuid

internal class DeleteStructureUseCaseImpl(
    private val structuresLocalDataSource: StructuresLocalDataSource,
) : DeleteStructureUseCase {
    override suspend operator fun invoke(structureId: Uuid) {
        logger.debug("Deleting structure {} from local storage.", structureId)
        structuresLocalDataSource.deleteStructure(structureId)
        logger.debug("Deleted structure {} from local storage.", structureId)
    }
}
