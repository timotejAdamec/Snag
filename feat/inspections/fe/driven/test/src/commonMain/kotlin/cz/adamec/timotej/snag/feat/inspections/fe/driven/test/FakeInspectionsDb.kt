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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlin.uuid.Uuid

class FakeInspectionsDb : InspectionsDb {
    private val inspections = MutableStateFlow<Map<Uuid, FrontendInspection>>(emptyMap())
    var forcedFailure: OfflineFirstDataResult.ProgrammerError? = null

    override fun getInspectionsFlow(projectId: Uuid): Flow<OfflineFirstDataResult<List<FrontendInspection>>> =
        inspections.map { map ->
            val failure = forcedFailure
            failure ?: OfflineFirstDataResult.Success(map.values.filter { it.inspection.projectId == projectId })
        }

    override suspend fun saveInspection(inspection: FrontendInspection): OfflineFirstDataResult<Unit> {
        val failure = forcedFailure
        if (failure != null) return failure

        inspections.update { it + (inspection.inspection.id to inspection) }
        return OfflineFirstDataResult.Success(Unit)
    }

    override suspend fun deleteInspection(id: Uuid): OfflineFirstDataResult<Unit> {
        val failure = forcedFailure
        if (failure != null) return failure

        inspections.update { it - id }
        return OfflineFirstDataResult.Success(Unit)
    }

    override fun getInspectionFlow(id: Uuid): Flow<OfflineFirstDataResult<FrontendInspection?>> =
        inspections.map { map ->
            val failure = forcedFailure
            if (failure != null) {
                failure
            } else {
                OfflineFirstDataResult.Success(map[id])
            }
        }

    override suspend fun getInspectionIdsByProjectId(projectId: Uuid): List<Uuid> {
        val matching = inspections.value.values.filter { it.inspection.projectId == projectId }
        return matching.map { it.inspection.id }
    }

    override suspend fun deleteInspectionsByProjectId(projectId: Uuid): OfflineFirstDataResult<Unit> {
        val failure = forcedFailure
        if (failure != null) return failure
        inspections.update { current -> current.filterValues { it.inspection.projectId != projectId } }
        return OfflineFirstDataResult.Success(Unit)
    }

    fun setInspection(inspection: FrontendInspection) {
        inspections.update { it + (inspection.inspection.id to inspection) }
    }

    fun setInspections(inspections: List<FrontendInspection>) {
        this.inspections.update { current ->
            current + inspections.associateBy { it.inspection.id }
        }
    }
}
