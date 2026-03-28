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
import cz.adamec.timotej.snag.structures.be.app.api.GetStructureUseCase
import cz.adamec.timotej.snag.structures.be.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.structures.be.ports.StructuresDb
import kotlin.uuid.Uuid

internal class GetStructureUseCaseImpl(
    private val structuresDb: StructuresDb,
) : GetStructureUseCase {
    override suspend operator fun invoke(id: Uuid): BackendStructure? {
        logger.debug("Getting structure {} from local storage.", id)
        return structuresDb.getStructure(id).also {
            logger.debug("Got structure {} from local storage.", id)
        }
    }
}
