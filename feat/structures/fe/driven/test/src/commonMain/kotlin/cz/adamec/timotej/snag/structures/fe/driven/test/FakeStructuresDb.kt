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
import cz.adamec.timotej.snag.lib.core.fe.test.FakeEntityDb
import cz.adamec.timotej.snag.structures.fe.ports.StructuresDb
import kotlin.uuid.Uuid

class FakeStructuresDb :
    FakeEntityDb<FrontendStructure>(
        getId = { it.structure.id },
    ),
    StructuresDb {
    override fun getStructuresFlow(projectId: Uuid) = allItemsFlow { it.structure.projectId == projectId }

    override fun getStructureFlow(id: Uuid) = itemByIdFlow(id)

    override suspend fun saveStructure(structure: FrontendStructure) = saveOneItem(structure)

    override suspend fun saveStructures(structures: List<FrontendStructure>) = saveManyItems(structures)

    override suspend fun deleteStructure(id: Uuid) = deleteItem(id)

    override suspend fun getStructureIdsByProjectId(projectId: Uuid): List<Uuid> =
        items.value.values
            .filter { it.structure.projectId == projectId }
            .map { it.structure.id }

    override suspend fun deleteStructuresByProjectId(projectId: Uuid) = deleteItemsWhere { it.structure.projectId != projectId }

    fun setStructure(structure: FrontendStructure) = setItem(structure)

    fun setStructures(structures: List<FrontendStructure>) = setItems(structures)
}
