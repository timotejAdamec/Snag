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

package cz.adamec.timotej.snag.structures.fe.app.impl.internal

import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.findings.fe.app.api.CascadeRestoreLocalFindingsByStructureIdUseCase
import cz.adamec.timotej.snag.structures.fe.app.api.CascadeRestoreLocalStructuresByProjectIdUseCase
import cz.adamec.timotej.snag.structures.fe.ports.StructuresApi
import cz.adamec.timotej.snag.structures.fe.ports.StructuresDb
import kotlin.uuid.Uuid

internal class CascadeRestoreLocalStructuresByProjectIdUseCaseImpl(
    private val structuresApi: StructuresApi,
    private val structuresDb: StructuresDb,
    private val cascadeRestoreLocalFindingsByStructureIdUseCase: CascadeRestoreLocalFindingsByStructureIdUseCase,
) : CascadeRestoreLocalStructuresByProjectIdUseCase {
    override suspend operator fun invoke(projectId: Uuid) {
        when (val result = structuresApi.getStructures(projectId)) {
            is OnlineDataResult.Success -> {
                structuresDb.saveStructures(result.data)
                result.data.forEach {
                    cascadeRestoreLocalFindingsByStructureIdUseCase(it.id)
                }
            }
            is OnlineDataResult.Failure -> {
                LH.logger.w { "Failed to restore structures for project $projectId: $result" }
            }
        }
    }
}
