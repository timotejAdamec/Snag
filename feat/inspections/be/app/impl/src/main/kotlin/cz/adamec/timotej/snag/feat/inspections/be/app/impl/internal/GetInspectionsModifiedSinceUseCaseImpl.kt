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

import cz.adamec.timotej.snag.feat.inspections.be.app.api.GetInspectionsModifiedSinceUseCase
import cz.adamec.timotej.snag.feat.inspections.be.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.feat.inspections.be.model.BackendInspection
import cz.adamec.timotej.snag.feat.inspections.be.ports.InspectionsDb
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import kotlin.uuid.Uuid

internal class GetInspectionsModifiedSinceUseCaseImpl(
    private val inspectionsDb: InspectionsDb,
) : GetInspectionsModifiedSinceUseCase {
    override suspend operator fun invoke(
        projectId: Uuid,
        since: Timestamp,
    ): List<BackendInspection> {
        logger.debug("Getting inspections modified since {} for project {} from local storage.", since, projectId)
        return inspectionsDb.getInspectionsModifiedSince(projectId, since).also {
            logger.debug(
                "Got {} inspections modified since {} for project {} from local storage.",
                it.size,
                since,
                projectId,
            )
        }
    }
}
