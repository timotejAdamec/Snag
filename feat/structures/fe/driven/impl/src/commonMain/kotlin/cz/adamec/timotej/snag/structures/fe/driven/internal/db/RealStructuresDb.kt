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

package cz.adamec.timotej.snag.structures.fe.driven.internal.db

import cz.adamec.timotej.snag.feat.structures.fe.model.FrontendStructure
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.structures.fe.ports.StructuresDb
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

internal class RealStructuresDb(
    private val ops: StructuresSqlDelightDbOps,
) : StructuresDb {
    override fun getStructuresFlow(projectId: Uuid): Flow<OfflineFirstDataResult<List<FrontendStructure>>> =
        ops.structuresByProjectIdFlow(projectId)

    override fun getStructureFlow(id: Uuid): Flow<OfflineFirstDataResult<FrontendStructure?>> = ops.entityByIdFlow(id)

    override suspend fun saveStructure(structure: FrontendStructure): OfflineFirstDataResult<Unit> = ops.saveOne(structure)

    override suspend fun saveStructures(structures: List<FrontendStructure>): OfflineFirstDataResult<Unit> = ops.saveMany(structures)

    override suspend fun deleteStructure(id: Uuid): OfflineFirstDataResult<Unit> = ops.deleteById(id)

    override suspend fun getStructureIdsByProjectId(projectId: Uuid): List<Uuid> = ops.getStructureIdsByProjectId(projectId)

    override suspend fun deleteStructuresByProjectId(projectId: Uuid): OfflineFirstDataResult<Unit> =
        ops.deleteStructuresByProjectId(projectId)
}
