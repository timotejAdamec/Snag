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

package cz.adamec.timotej.snag.structures.fe.app.impl.internal

import cz.adamec.timotej.snag.feat.structures.business.Structure
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.structures.fe.app.api.GetStructureUseCase
import cz.adamec.timotej.snag.structures.fe.ports.StructuresDb
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.uuid.Uuid

class GetStructureUseCaseImpl(
    private val structuresDb: StructuresDb,
) : GetStructureUseCase {
    override operator fun invoke(structureId: Uuid): Flow<OfflineFirstDataResult<Structure?>> =
        structuresDb.getStructureFlow(structureId)
            .distinctUntilChanged()
}
