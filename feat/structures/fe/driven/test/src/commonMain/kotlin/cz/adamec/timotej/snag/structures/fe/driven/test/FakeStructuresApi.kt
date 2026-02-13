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
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.fe.test.FakeEntityApi
import cz.adamec.timotej.snag.structures.fe.ports.StructureSyncResult
import cz.adamec.timotej.snag.structures.fe.ports.StructuresApi
import kotlin.uuid.Uuid

class FakeStructuresApi :
    FakeEntityApi<FrontendStructure, StructureSyncResult>(
        getId = { it.structure.id },
    ),
    StructuresApi {
    var saveStructureResponseOverride
        get() = saveResponseOverride
        set(value) {
            saveResponseOverride = value
        }

    override suspend fun getStructures(projectId: Uuid) = getAllItems { it.structure.projectId == projectId }

    override suspend fun saveStructure(frontendStructure: FrontendStructure) = saveItem(frontendStructure)

    override suspend fun deleteStructure(
        id: Uuid,
        deletedAt: Timestamp,
    ) = deleteItemById(id)

    override suspend fun getStructuresModifiedSince(
        projectId: Uuid,
        since: Timestamp,
    ) = getModifiedSinceItems()

    fun setStructure(structure: FrontendStructure) = setItem(structure)

    fun setStructures(structures: List<FrontendStructure>) = setItems(structures)
}
