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

import cz.adamec.timotej.snag.feat.inspections.fe.app.api.GetInspectionUseCase
import cz.adamec.timotej.snag.feat.inspections.fe.model.FrontendInspection
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsDb
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.uuid.Uuid

class GetInspectionUseCaseImpl(
    private val inspectionsDb: InspectionsDb,
) : GetInspectionUseCase {
    override operator fun invoke(inspectionId: Uuid): Flow<OfflineFirstDataResult<FrontendInspection?>> {
        val flow = inspectionsDb.getInspectionFlow(inspectionId)
        return flow.distinctUntilChanged()
    }
}
