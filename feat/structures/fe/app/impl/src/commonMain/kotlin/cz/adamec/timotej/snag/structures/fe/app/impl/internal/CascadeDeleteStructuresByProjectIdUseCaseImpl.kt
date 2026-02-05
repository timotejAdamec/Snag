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

import cz.adamec.timotej.snag.findings.fe.app.api.DeleteFindingsByStructureIdUseCase
import cz.adamec.timotej.snag.structures.fe.app.api.CascadeDeleteStructuresByProjectIdUseCase
import cz.adamec.timotej.snag.structures.fe.ports.StructuresDb
import kotlin.uuid.Uuid

internal class CascadeDeleteStructuresByProjectIdUseCaseImpl(
    private val structuresDb: StructuresDb,
    private val deleteFindingsByStructureIdUseCase: DeleteFindingsByStructureIdUseCase,
) : CascadeDeleteStructuresByProjectIdUseCase {
    override suspend operator fun invoke(projectId: Uuid) {
        val structureIds = structuresDb.getStructureIdsByProjectId(projectId)
        structureIds.forEach { structureId ->
            deleteFindingsByStructureIdUseCase(structureId)
        }
        structuresDb.deleteStructuresByProjectId(projectId)
    }
}
