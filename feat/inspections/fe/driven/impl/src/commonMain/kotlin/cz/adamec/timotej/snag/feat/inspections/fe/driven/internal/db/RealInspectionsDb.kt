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

package cz.adamec.timotej.snag.feat.inspections.fe.driven.internal.db

import cz.adamec.timotej.snag.feat.inspections.fe.model.FrontendInspection
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsDb
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

internal class RealInspectionsDb(
    private val ops: InspectionsSqlDelightDbOps,
) : InspectionsDb {
    override fun getInspectionsFlow(projectId: Uuid): Flow<OfflineFirstDataResult<List<FrontendInspection>>> =
        ops.inspectionsByProjectIdFlow(projectId)

    override suspend fun saveInspection(inspection: FrontendInspection): OfflineFirstDataResult<Unit> = ops.saveOne(inspection)

    override suspend fun deleteInspection(id: Uuid): OfflineFirstDataResult<Unit> = ops.deleteById(id)

    override fun getInspectionFlow(id: Uuid): Flow<OfflineFirstDataResult<FrontendInspection?>> = ops.entityByIdFlow(id)

    override suspend fun getInspectionIdsByProjectId(projectId: Uuid): List<Uuid> = ops.getInspectionIdsByProjectId(projectId)

    override suspend fun deleteInspectionsByProjectId(projectId: Uuid): OfflineFirstDataResult<Unit> =
        ops.deleteInspectionsByProjectId(projectId)
}
