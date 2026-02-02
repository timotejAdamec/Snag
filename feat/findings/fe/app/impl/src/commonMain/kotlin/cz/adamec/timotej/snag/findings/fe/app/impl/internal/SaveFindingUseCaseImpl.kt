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

import cz.adamec.timotej.snag.feat.findings.business.Coordinate
import cz.adamec.timotej.snag.feat.findings.business.Finding
import cz.adamec.timotej.snag.findings.fe.app.api.SaveFindingUseCase
import cz.adamec.timotej.snag.findings.fe.app.api.model.SaveFindingRequest
import cz.adamec.timotej.snag.findings.fe.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.findings.fe.ports.FindingsDb
import cz.adamec.timotej.snag.findings.fe.ports.FindingsSync
import cz.adamec.timotej.snag.lib.core.common.UuidProvider
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.core.fe.log
import cz.adamec.timotej.snag.lib.core.fe.map
import kotlinx.coroutines.flow.firstOrNull
import kotlin.uuid.Uuid

class SaveFindingUseCaseImpl(
    private val findingsDb: FindingsDb,
    private val findingsSync: FindingsSync,
    private val uuidProvider: UuidProvider,
) : SaveFindingUseCase {
    override suspend operator fun invoke(request: SaveFindingRequest): OfflineFirstDataResult<Uuid> {
        val coordinatesResult = resolveCoordinates(request)
        if (coordinatesResult is OfflineFirstDataResult.ProgrammerError) return coordinatesResult

        val coordinates = (coordinatesResult as OfflineFirstDataResult.Success).data

        val finding =
            Finding(
                id = request.id ?: uuidProvider.getUuid(),
                structureId = request.structureId,
                name = request.name,
                description = request.description,
                coordinates = coordinates,
            )

        return findingsDb
            .saveFinding(finding)
            .also {
                logger.log(
                    offlineFirstDataResult = it,
                    additionalInfo = "SaveFindingUseCase, findingsDb.saveFinding($finding)",
                )
                if (it is OfflineFirstDataResult.Success) {
                    findingsSync.enqueueFindingSave(finding.id)
                }
            }.map {
                finding.id
            }
    }

    private suspend fun resolveCoordinates(request: SaveFindingRequest): OfflineFirstDataResult<List<Coordinate>> {
        val requestCoordinates = request.coordinates
        if (requestCoordinates != null) {
            return OfflineFirstDataResult.Success(requestCoordinates)
        }

        val findingId = request.id ?: return OfflineFirstDataResult.Success(emptyList())

        val existingResult = findingsDb.getFindingFlow(findingId).firstOrNull()
            ?: return OfflineFirstDataResult.ProgrammerError(
                IllegalStateException("No emission from getFindingFlow for finding $findingId"),
            )

        return when (existingResult) {
            is OfflineFirstDataResult.Success -> {
                val coordinates = existingResult.data?.coordinates.orEmpty()
                OfflineFirstDataResult.Success(coordinates)
            }
            is OfflineFirstDataResult.ProgrammerError -> existingResult
        }
    }
}
