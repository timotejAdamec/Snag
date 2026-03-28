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
import cz.adamec.timotej.snag.findings.be.app.api.GetFindingUseCase
import cz.adamec.timotej.snag.findings.be.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.findings.be.ports.FindingsDb
import kotlin.uuid.Uuid

internal class GetFindingUseCaseImpl(
    private val findingsDb: FindingsDb,
) : GetFindingUseCase {
    override suspend operator fun invoke(id: Uuid): BackendFinding? {
        logger.debug("Getting finding {} from local storage.", id)
        return findingsDb.getFinding(id).also {
            logger.debug("Got finding {} from local storage.", id)
        }
    }
}
