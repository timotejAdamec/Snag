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
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionSyncResult
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsApi
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import kotlin.uuid.Uuid

class FakeInspectionsApi : InspectionsApi {
    private val inspections = mutableMapOf<Uuid, FrontendInspection>()
    var forcedFailure: OnlineDataResult.Failure? = null
    var saveInspectionResponseOverride: ((FrontendInspection) -> OnlineDataResult<FrontendInspection?>)? = null
    var modifiedSinceResults: List<InspectionSyncResult> = emptyList()

    override suspend fun getInspections(projectId: Uuid): OnlineDataResult<List<FrontendInspection>> {
        val failure = forcedFailure
        if (failure != null) return failure
        return OnlineDataResult.Success(inspections.values.filter { it.inspection.projectId == projectId })
    }

    override suspend fun deleteInspection(
        id: Uuid,
        deletedAt: Timestamp,
    ): OnlineDataResult<Unit> {
        val failure = forcedFailure
        if (failure != null) return failure
        inspections.remove(id)
        return OnlineDataResult.Success(Unit)
    }

    override suspend fun saveInspection(frontendInspection: FrontendInspection): OnlineDataResult<FrontendInspection?> {
        val failure = forcedFailure
        if (failure != null) return failure
        val override = saveInspectionResponseOverride
        return if (override != null) {
            override(frontendInspection)
        } else {
            inspections[frontendInspection.inspection.id] = frontendInspection
            OnlineDataResult.Success(frontendInspection)
        }
    }

    fun setInspection(inspection: FrontendInspection) {
        inspections[inspection.inspection.id] = inspection
    }

    override suspend fun getInspectionsModifiedSince(
        projectId: Uuid,
        since: Timestamp,
    ): OnlineDataResult<List<InspectionSyncResult>> {
        val failure = forcedFailure
        if (failure != null) return failure
        return OnlineDataResult.Success(modifiedSinceResults)
    }

    fun setInspections(inspections: List<FrontendInspection>) {
        inspections.forEach { this.inspections[it.inspection.id] = it }
    }
}
