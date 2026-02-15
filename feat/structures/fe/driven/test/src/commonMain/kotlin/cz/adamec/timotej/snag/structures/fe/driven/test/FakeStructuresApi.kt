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
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.network.fe.test.FakeApiOps
import cz.adamec.timotej.snag.structures.fe.ports.StructureSyncResult
import cz.adamec.timotej.snag.structures.fe.ports.StructuresApi
import kotlin.uuid.Uuid

class FakeStructuresApi : StructuresApi {
    private val ops =
        FakeApiOps<FrontendStructure, StructureSyncResult>(getId = { it.structure.id })

    var forcedFailure
        get() = ops.forcedFailure
        set(value) {
            ops.forcedFailure = value
        }

    var saveStructureResponseOverride
        get() = ops.saveResponseOverride
        set(value) {
            ops.saveResponseOverride = value
        }

    var modifiedSinceResults
        get() = ops.modifiedSinceResults
        set(value) {
            ops.modifiedSinceResults = value
        }

    override suspend fun getStructures(projectId: Uuid): OnlineDataResult<List<FrontendStructure>> =
        ops.getAllItems { it.structure.projectId == projectId }

    override suspend fun saveStructure(frontendStructure: FrontendStructure): OnlineDataResult<FrontendStructure?> =
        ops.saveItem(frontendStructure)

    override suspend fun deleteStructure(
        id: Uuid,
        deletedAt: Timestamp,
    ): OnlineDataResult<Unit> = ops.deleteItemById(id)

    override suspend fun getStructuresModifiedSince(
        projectId: Uuid,
        since: Timestamp,
    ): OnlineDataResult<List<StructureSyncResult>> = ops.getModifiedSinceItems()

    fun setStructure(structure: FrontendStructure) = ops.setItem(structure)

    fun setStructures(structures: List<FrontendStructure>) = ops.setItems(structures)
}
