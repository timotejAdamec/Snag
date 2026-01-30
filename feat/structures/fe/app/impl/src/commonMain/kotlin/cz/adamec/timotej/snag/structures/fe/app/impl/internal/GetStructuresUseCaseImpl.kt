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

import cz.adamec.timotej.snag.feat.structures.business.Structure
import cz.adamec.timotej.snag.lib.core.common.ApplicationScope
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.lib.core.fe.log
import cz.adamec.timotej.snag.structures.fe.app.api.GetStructuresUseCase
import cz.adamec.timotej.snag.structures.fe.ports.StructuresApi
import cz.adamec.timotej.snag.structures.fe.ports.StructuresDb
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

internal class GetStructuresUseCaseImpl(
    private val structuresApi: StructuresApi,
    private val structuresDb: StructuresDb,
    private val applicationScope: ApplicationScope,
) : GetStructuresUseCase {
    override operator fun invoke(projectId: Uuid): Flow<OfflineFirstDataResult<List<Structure>>> {
        applicationScope.launch {
            when (val remoteStructuresResult = structuresApi.getStructures(projectId)) {
                is OnlineDataResult.Failure ->
                    LH.logger.w(
                        "Error fetching structures for project $projectId, not updating local DB.",
                    )
                is OnlineDataResult.Success -> {
                    LH.logger.d {
                        "Fetched ${remoteStructuresResult.data.size} structures for project $projectId from API." +
                            " Saving them to local DB."
                    }
                    structuresDb.saveStructures(remoteStructuresResult.data)
                }
            }
        }

        return structuresDb
            .getStructuresFlow(projectId)
            .onEach {
                LH.logger.log(
                    offlineFirstDataResult = it,
                    additionalInfo = "GetStructuresUseCase, structuresDb.getStructuresFlow($projectId)",
                )
            }.distinctUntilChanged()
    }
}
