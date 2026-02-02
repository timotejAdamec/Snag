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

package cz.adamec.timotej.snag.findings.fe.app.impl.internal

import cz.adamec.timotej.snag.feat.findings.business.Finding
import cz.adamec.timotej.snag.findings.fe.app.api.SaveNewFindingUseCase
import cz.adamec.timotej.snag.findings.fe.app.api.model.SaveNewFindingRequest
import cz.adamec.timotej.snag.findings.fe.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.findings.fe.ports.FindingsDb
import cz.adamec.timotej.snag.findings.fe.ports.FindingsSync
import cz.adamec.timotej.snag.lib.core.common.UuidProvider
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.core.fe.log
import cz.adamec.timotej.snag.lib.core.fe.map
import kotlin.uuid.Uuid

class SaveNewFindingUseCaseImpl(
    private val findingsDb: FindingsDb,
    private val findingsSync: FindingsSync,
    private val uuidProvider: UuidProvider,
) : SaveNewFindingUseCase {
    override suspend operator fun invoke(request: SaveNewFindingRequest): OfflineFirstDataResult<Uuid> {
        val finding =
            Finding(
                id = uuidProvider.getUuid(),
                structureId = request.structureId,
                name = request.name,
                description = request.description,
                coordinates = request.coordinates,
            )

        return findingsDb
            .saveFinding(finding)
            .also {
                logger.log(
                    offlineFirstDataResult = it,
                    additionalInfo = "SaveNewFindingUseCase, findingsDb.saveFinding($finding)",
                )
                if (it is OfflineFirstDataResult.Success) {
                    findingsSync.enqueueFindingSave(finding.id)
                }
            }.map {
                finding.id
            }
    }
}
