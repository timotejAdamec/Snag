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

import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.feat.inspections.app.model.AppInspection
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsDb
import cz.adamec.timotej.snag.lib.database.fe.test.FakeDbOps
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

class FakeInspectionsDb : InspectionsDb {
    private val ops = FakeDbOps<AppInspection>(getId = { it.id })

    var forcedFailure
        get() = ops.forcedFailure
        set(value) {
            ops.forcedFailure = value
        }

    override fun getInspectionsFlow(projectId: Uuid): Flow<OfflineFirstDataResult<List<AppInspection>>> =
        ops.allItemsFlow { it.projectId == projectId }

    override suspend fun saveInspection(inspection: AppInspection): OfflineFirstDataResult<Unit> = ops.saveOneItem(inspection)

    override suspend fun deleteInspection(id: Uuid): OfflineFirstDataResult<Unit> = ops.deleteItem(id)

    override fun getInspectionFlow(id: Uuid): Flow<OfflineFirstDataResult<AppInspection?>> = ops.itemByIdFlow(id)

    override suspend fun getInspectionIdsByProjectId(projectId: Uuid): List<Uuid> =
        ops.items.value.values
            .filter { it.projectId == projectId }
            .map { it.id }

    override suspend fun deleteInspectionsByProjectId(projectId: Uuid): OfflineFirstDataResult<Unit> =
        ops.deleteItemsWhere { it.projectId != projectId }

    fun setInspection(inspection: AppInspection) = ops.setItem(inspection)

    fun setInspections(inspections: List<AppInspection>) = ops.setItems(inspections)
}
