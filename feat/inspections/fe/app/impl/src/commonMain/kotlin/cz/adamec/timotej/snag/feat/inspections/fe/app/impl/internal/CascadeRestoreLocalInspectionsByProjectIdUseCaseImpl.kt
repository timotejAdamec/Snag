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

package cz.adamec.timotej.snag.feat.inspections.fe.app.impl.internal

import cz.adamec.timotej.snag.feat.inspections.fe.app.api.CascadeRestoreLocalInspectionsByProjectIdUseCase
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsApi
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsDb
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import kotlin.uuid.Uuid

internal class CascadeRestoreLocalInspectionsByProjectIdUseCaseImpl(
    private val inspectionsApi: InspectionsApi,
    private val inspectionsDb: InspectionsDb,
) : CascadeRestoreLocalInspectionsByProjectIdUseCase {
    override suspend operator fun invoke(projectId: Uuid) {
        when (val result = inspectionsApi.getInspections(projectId)) {
            is OnlineDataResult.Success ->
                result.data.forEach { inspectionsDb.saveInspection(it) }
            is OnlineDataResult.Failure ->
                LH.logger.w { "Failed to restore inspections for project $projectId: $result" }
        }
    }
}
