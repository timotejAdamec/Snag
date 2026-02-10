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

package cz.adamec.timotej.snag.feat.inspections.fe.ports

import cz.adamec.timotej.snag.feat.inspections.fe.model.FrontendInspection
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

interface InspectionsDb {
    fun getInspectionsFlow(projectId: Uuid): Flow<OfflineFirstDataResult<List<FrontendInspection>>>

    suspend fun saveInspection(inspection: FrontendInspection): OfflineFirstDataResult<Unit>

    fun getInspectionFlow(id: Uuid): Flow<OfflineFirstDataResult<FrontendInspection?>>

    suspend fun deleteInspection(id: Uuid): OfflineFirstDataResult<Unit>

    suspend fun getInspectionIdsByProjectId(projectId: Uuid): List<Uuid>

    suspend fun deleteInspectionsByProjectId(projectId: Uuid): OfflineFirstDataResult<Unit>
}
