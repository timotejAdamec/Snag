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

import cz.adamec.timotej.snag.feat.inspections.be.app.api.GetInspectionUseCase
import cz.adamec.timotej.snag.feat.inspections.be.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.feat.inspections.be.model.BackendInspection
import cz.adamec.timotej.snag.feat.inspections.be.ports.InspectionsDb
import kotlin.uuid.Uuid

internal class GetInspectionUseCaseImpl(
    private val inspectionsDb: InspectionsDb,
) : GetInspectionUseCase {
    override suspend operator fun invoke(id: Uuid): BackendInspection? {
        logger.debug("Getting inspection {} from local storage.", id)
        return inspectionsDb.getInspection(id).also {
            logger.debug("Got inspection {} from local storage.", id)
        }
    }
}
