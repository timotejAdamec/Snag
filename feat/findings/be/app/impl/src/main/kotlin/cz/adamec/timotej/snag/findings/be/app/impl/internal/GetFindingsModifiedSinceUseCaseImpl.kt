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

package cz.adamec.timotej.snag.findings.be.app.impl.internal

import cz.adamec.timotej.snag.feat.findings.be.model.BackendFinding
import cz.adamec.timotej.snag.findings.be.app.api.GetFindingsModifiedSinceUseCase
import cz.adamec.timotej.snag.findings.be.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.findings.be.ports.FindingsDb
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import kotlin.uuid.Uuid

internal class GetFindingsModifiedSinceUseCaseImpl(
    private val findingsDb: FindingsDb,
) : GetFindingsModifiedSinceUseCase {
    override suspend operator fun invoke(structureId: Uuid, since: Timestamp): List<BackendFinding> {
        logger.debug("Getting findings modified since {} for structure {} from local storage.", since, structureId)
        return findingsDb.getFindingsModifiedSince(structureId, since).also {
            logger.debug("Got {} findings modified since {} for structure {} from local storage.", it.size, since, structureId)
        }
    }
}
