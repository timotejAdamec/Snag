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

import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.findings.fe.app.api.CascadeRestoreLocalFindingsByStructureIdUseCase
import cz.adamec.timotej.snag.findings.fe.ports.FindingsApi
import cz.adamec.timotej.snag.findings.fe.ports.FindingsDb
import kotlin.uuid.Uuid

internal class CascadeRestoreLocalFindingsByStructureIdUseCaseImpl(
    private val findingsApi: FindingsApi,
    private val findingsDb: FindingsDb,
) : CascadeRestoreLocalFindingsByStructureIdUseCase {
    override suspend operator fun invoke(structureId: Uuid) {
        when (val result = findingsApi.getFindings(structureId)) {
            is OnlineDataResult.Success -> findingsDb.saveFindings(result.data)
            is OnlineDataResult.Failure ->
                LH.logger.w { "Failed to restore findings for structure $structureId: $result" }
        }
    }
}
