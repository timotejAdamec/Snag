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

import cz.adamec.timotej.snag.feat.inspections.fe.app.api.DeleteInspectionUseCase
import cz.adamec.timotej.snag.feat.inspections.fe.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsDb
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsSync
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.core.fe.log
import kotlin.uuid.Uuid

class DeleteInspectionUseCaseImpl(
    private val inspectionsDb: InspectionsDb,
    private val inspectionsSync: InspectionsSync,
) : DeleteInspectionUseCase {
    override suspend operator fun invoke(inspectionId: Uuid): OfflineFirstDataResult<Unit> =
        inspectionsDb
            .deleteInspection(inspectionId)
            .also {
                logger.log(
                    offlineFirstDataResult = it,
                    additionalInfo = "deleteInspection, inspectionsDb.deleteInspection($inspectionId)",
                )
                if (it is OfflineFirstDataResult.Success) {
                    inspectionsSync.enqueueInspectionDelete(inspectionId)
                }
            }
}
