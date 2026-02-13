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

package cz.adamec.timotej.snag.structures.fe.driven.test

import cz.adamec.timotej.snag.feat.structures.fe.model.FrontendStructure
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.database.fe.test.FakeDbOps
import cz.adamec.timotej.snag.structures.fe.ports.StructuresDb
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

class FakeStructuresDb : StructuresDb {
    private val ops = FakeDbOps<FrontendStructure>(getId = { it.structure.id })

    var forcedFailure
        get() = ops.forcedFailure
        set(value) {
            ops.forcedFailure = value
        }

    override fun getStructuresFlow(projectId: Uuid): Flow<OfflineFirstDataResult<List<FrontendStructure>>> =
        ops.allItemsFlow { it.structure.projectId == projectId }

    override fun getStructureFlow(id: Uuid): Flow<OfflineFirstDataResult<FrontendStructure?>> =
        ops.itemByIdFlow(id)

    override suspend fun saveStructure(structure: FrontendStructure): OfflineFirstDataResult<Unit> =
        ops.saveOneItem(structure)

    override suspend fun saveStructures(structures: List<FrontendStructure>): OfflineFirstDataResult<Unit> =
        ops.saveManyItems(structures)

    override suspend fun deleteStructure(id: Uuid): OfflineFirstDataResult<Unit> =
        ops.deleteItem(id)

    override suspend fun getStructureIdsByProjectId(projectId: Uuid): List<Uuid> =
        ops.items.value.values
            .filter { it.structure.projectId == projectId }
            .map { it.structure.id }

    override suspend fun deleteStructuresByProjectId(projectId: Uuid): OfflineFirstDataResult<Unit> =
        ops.deleteItemsWhere { it.structure.projectId != projectId }

    fun setStructure(structure: FrontendStructure) = ops.setItem(structure)

    fun setStructures(structures: List<FrontendStructure>) = ops.setItems(structures)
}
