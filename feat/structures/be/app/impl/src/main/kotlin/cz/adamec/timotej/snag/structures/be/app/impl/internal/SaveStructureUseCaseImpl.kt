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
import cz.adamec.timotej.snag.structures.be.app.api.SaveStructureUseCase
import cz.adamec.timotej.snag.structures.be.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.structures.be.ports.StructuresDb

internal class SaveStructureUseCaseImpl(
    private val structuresDb: StructuresDb,
) : SaveStructureUseCase {
    override suspend operator fun invoke(backendStructure: BackendStructure): BackendStructure? {
        logger.debug("Saving structure {} to local storage.", backendStructure)
        return structuresDb.saveStructure(backendStructure).also {
            logger.debug("Saved structure {} to local storage.", backendStructure)
        }
    }
}
