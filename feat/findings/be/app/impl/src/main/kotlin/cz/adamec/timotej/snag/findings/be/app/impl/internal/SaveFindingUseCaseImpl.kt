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
import cz.adamec.timotej.snag.findings.be.app.api.SaveFindingUseCase
import cz.adamec.timotej.snag.findings.be.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.findings.be.ports.FindingsDb
import cz.adamec.timotej.snag.lib.sync.be.SaveConflictResult
import cz.adamec.timotej.snag.lib.sync.be.resolveConflictForSave

internal class SaveFindingUseCaseImpl(
    private val findingsDb: FindingsDb,
) : SaveFindingUseCase {
    override suspend operator fun invoke(finding: BackendFinding): BackendFinding? {
        logger.debug("Saving finding {} to local storage.", finding)
        val existing = findingsDb.getFinding(finding.id)
        return when (val result = resolveConflictForSave(existing, finding)) {
            is SaveConflictResult.Proceed -> {
                findingsDb.upsertFinding(finding)
                logger.debug("Saved finding {} to local storage.", finding)
                null
            }
            is SaveConflictResult.Rejected -> {
                logger.debug(
                    "Didn't save finding {} to local storage as there is a newer one." +
                        " Returning the newer one ({}).",
                    finding,
                    result.serverVersion,
                )
                result.serverVersion
            }
        }
    }
}
