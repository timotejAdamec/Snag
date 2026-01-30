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
import cz.adamec.timotej.snag.structures.be.app.api.SaveStructureUseCase
import cz.adamec.timotej.snag.structures.be.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.structures.be.ports.StructuresLocalDataSource

internal class SaveStructureUseCaseImpl(
    private val structuresLocalDataSource: StructuresLocalDataSource,
) : SaveStructureUseCase {
    override suspend operator fun invoke(structure: Structure): Structure? {
        logger.debug("Saving structure {} to local storage.", structure)
        return structuresLocalDataSource.updateStructure(structure).also {
            logger.debug("Saved structure {} to local storage.", structure)
        }
    }
}
