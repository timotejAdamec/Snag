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
import cz.adamec.timotej.snag.network.fe.test.FakeApiOps
import kotlin.uuid.Uuid

class FakeInspectionsApi : InspectionsApi {
    private val ops =
        FakeApiOps<FrontendInspection, InspectionSyncResult>(getId = { it.inspection.id })

    var forcedFailure
        get() = ops.forcedFailure
        set(value) {
            ops.forcedFailure = value
        }

    var saveInspectionResponseOverride
        get() = ops.saveResponseOverride
        set(value) {
            ops.saveResponseOverride = value
        }

    var modifiedSinceResults
        get() = ops.modifiedSinceResults
        set(value) {
            ops.modifiedSinceResults = value
        }

    override suspend fun getInspections(projectId: Uuid): OnlineDataResult<List<FrontendInspection>> =
        ops.getAllItems { it.inspection.projectId == projectId }

    override suspend fun deleteInspection(
        id: Uuid,
        deletedAt: Timestamp,
    ): OnlineDataResult<Unit> = ops.deleteItemById(id)

    override suspend fun saveInspection(frontendInspection: FrontendInspection): OnlineDataResult<FrontendInspection?> =
        ops.saveItem(frontendInspection)

    override suspend fun getInspectionsModifiedSince(
        projectId: Uuid,
        since: Timestamp,
    ): OnlineDataResult<List<InspectionSyncResult>> = ops.getModifiedSinceItems()

    fun setInspection(inspection: FrontendInspection) = ops.setItem(inspection)

    fun setInspections(inspections: List<FrontendInspection>) = ops.setItems(inspections)
}
