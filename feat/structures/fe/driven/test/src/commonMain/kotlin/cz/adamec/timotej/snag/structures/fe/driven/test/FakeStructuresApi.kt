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

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.feat.structures.app.model.AppStructure
import cz.adamec.timotej.snag.network.fe.test.FakeApiOps
import cz.adamec.timotej.snag.structures.fe.ports.StructureSyncResult
import cz.adamec.timotej.snag.structures.fe.ports.StructuresApi
import kotlin.uuid.Uuid

class FakeStructuresApi : StructuresApi {
    private val ops =
        FakeApiOps<AppStructure, StructureSyncResult>(getId = { it.id })

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

    override suspend fun getStructures(projectId: Uuid): OnlineDataResult<List<AppStructure>> =
        ops.getAllItems { it.projectId == projectId }

    override suspend fun saveStructure(frontendStructure: AppStructure): OnlineDataResult<AppStructure?> = ops.saveItem(frontendStructure)

    override suspend fun deleteStructure(
        id: Uuid,
        deletedAt: Timestamp,
    ): OnlineDataResult<AppStructure?> = ops.deleteItemById(id)

    override suspend fun getStructuresModifiedSince(
        projectId: Uuid,
        since: Timestamp,
    ): OnlineDataResult<List<StructureSyncResult>> = ops.getModifiedSinceItems()

    fun setStructure(structure: AppStructure) = ops.setItem(structure)

    fun setStructures(structures: List<AppStructure>) = ops.setItems(structures)
}
