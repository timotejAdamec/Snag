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

import cz.adamec.timotej.snag.feat.structures.fe.model.FrontendStructure
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

interface StructuresDb {
    fun getStructuresFlow(projectId: Uuid): Flow<OfflineFirstDataResult<List<FrontendStructure>>>

    suspend fun saveStructures(structures: List<FrontendStructure>): OfflineFirstDataResult<Unit>

    suspend fun saveStructure(structure: FrontendStructure): OfflineFirstDataResult<Unit>

    fun getStructureFlow(id: Uuid): Flow<OfflineFirstDataResult<FrontendStructure?>>

    suspend fun deleteStructure(id: Uuid): OfflineFirstDataResult<Unit>
}
