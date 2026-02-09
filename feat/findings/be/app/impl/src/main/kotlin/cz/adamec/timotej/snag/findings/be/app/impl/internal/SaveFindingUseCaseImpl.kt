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

internal class SaveFindingUseCaseImpl(
    private val findingsDb: FindingsDb,
) : SaveFindingUseCase {
    override suspend operator fun invoke(finding: BackendFinding): BackendFinding? {
        logger.debug("Saving finding {} to local storage.", finding)
        return findingsDb.saveFinding(finding).also {
            it?.let {
                logger.debug(
                    "Didn't save finding {} to local storage as there is a newer one." +
                        " Returning the newer one ({}).",
                    finding,
                    it,
                )
            } ?: logger.debug("Saved finding {} to local storage.", finding)
        }
    }
}
