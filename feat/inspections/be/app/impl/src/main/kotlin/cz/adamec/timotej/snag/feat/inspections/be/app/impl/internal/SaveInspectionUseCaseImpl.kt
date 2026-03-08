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

package cz.adamec.timotej.snag.feat.inspections.be.app.impl.internal

import cz.adamec.timotej.snag.feat.inspections.be.app.api.SaveInspectionUseCase
import cz.adamec.timotej.snag.feat.inspections.be.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.feat.inspections.be.model.BackendInspection
import cz.adamec.timotej.snag.feat.inspections.be.ports.InspectionsDb
import cz.adamec.timotej.snag.lib.sync.be.SaveConflictResult
import cz.adamec.timotej.snag.lib.sync.be.resolveConflictForSave

internal class SaveInspectionUseCaseImpl(
    private val inspectionsDb: InspectionsDb,
) : SaveInspectionUseCase {
    override suspend operator fun invoke(backendInspection: BackendInspection): BackendInspection? {
        logger.debug("Saving inspection {} to local storage.", backendInspection)
        val existing = inspectionsDb.getInspection(backendInspection.id)
        return when (val result = resolveConflictForSave(existing, backendInspection)) {
            is SaveConflictResult.Proceed -> {
                inspectionsDb.upsertInspection(backendInspection)
                logger.debug("Saved inspection {} to local storage.", backendInspection)
                null
            }
            is SaveConflictResult.Rejected -> {
                logger.debug(
                    "Didn't save inspection {} to local storage as there is a newer one." +
                        " Returning the newer one ({}).",
                    backendInspection,
                    result.serverVersion,
                )
                result.serverVersion
            }
        }
    }
}
