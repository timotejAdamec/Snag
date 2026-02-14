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

internal class SaveInspectionUseCaseImpl(
    private val inspectionsDb: InspectionsDb,
) : SaveInspectionUseCase {
    override suspend operator fun invoke(backendInspection: BackendInspection): BackendInspection? {
        logger.debug("Saving inspection {} to local storage.", backendInspection)
        return inspectionsDb.saveInspection(backendInspection).also {
            it?.let {
                logger.debug(
                    "Didn't save inspection {} to local storage as there is a newer one." +
                        " Returning the newer one ({}).",
                    backendInspection,
                    it,
                )
            } ?: logger.debug("Saved inspection {} to local storage.", backendInspection)
        }
    }
}
