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

package cz.adamec.timotej.snag.structures.fe.ports

import cz.adamec.timotej.snag.feat.structures.app.model.AppStructure
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import kotlin.uuid.Uuid

sealed interface StructureSyncResult {
    data class Deleted(
        val id: Uuid,
    ) : StructureSyncResult

    data class Updated(
        val structure: AppStructure,
    ) : StructureSyncResult
}

interface StructuresApi {
    suspend fun getStructures(projectId: Uuid): OnlineDataResult<List<AppStructure>>

    suspend fun saveStructure(appStructure: AppStructure): OnlineDataResult<AppStructure?>

    suspend fun deleteStructure(
        id: Uuid,
        deletedAt: Timestamp,
    ): OnlineDataResult<AppStructure?>

    suspend fun getStructuresModifiedSince(
        projectId: Uuid,
        since: Timestamp,
    ): OnlineDataResult<List<StructureSyncResult>>
}
