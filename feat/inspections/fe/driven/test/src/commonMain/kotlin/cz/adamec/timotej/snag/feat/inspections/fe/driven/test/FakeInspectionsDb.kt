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

package cz.adamec.timotej.snag.feat.inspections.fe.driven.test

import cz.adamec.timotej.snag.feat.inspections.fe.model.FrontendInspection
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsDb
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.database.fe.test.FakeDbOps
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

class FakeInspectionsDb : InspectionsDb {
    private val ops = FakeDbOps<FrontendInspection>(getId = { it.inspection.id })

    var forcedFailure
        get() = ops.forcedFailure
        set(value) {
            ops.forcedFailure = value
        }

    override fun getInspectionsFlow(projectId: Uuid): Flow<OfflineFirstDataResult<List<FrontendInspection>>> =
        ops.allItemsFlow { it.inspection.projectId == projectId }

    override suspend fun saveInspection(inspection: FrontendInspection): OfflineFirstDataResult<Unit> = ops.saveOneItem(inspection)

    override suspend fun deleteInspection(id: Uuid): OfflineFirstDataResult<Unit> = ops.deleteItem(id)

    override fun getInspectionFlow(id: Uuid): Flow<OfflineFirstDataResult<FrontendInspection?>> = ops.itemByIdFlow(id)

    override suspend fun getInspectionIdsByProjectId(projectId: Uuid): List<Uuid> =
        ops.items.value.values
            .filter { it.inspection.projectId == projectId }
            .map { it.inspection.id }

    override suspend fun deleteInspectionsByProjectId(projectId: Uuid): OfflineFirstDataResult<Unit> =
        ops.deleteItemsWhere { it.inspection.projectId != projectId }

    fun setInspection(inspection: FrontendInspection) = ops.setItem(inspection)

    fun setInspections(inspections: List<FrontendInspection>) = ops.setItems(inspections)
}
