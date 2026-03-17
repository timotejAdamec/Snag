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

import cz.adamec.timotej.snag.core.foundation.common.ApplicationScope
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.core.network.fe.log
import cz.adamec.timotej.snag.feat.structures.app.model.AppStructure
import cz.adamec.timotej.snag.structures.fe.app.api.GetStructuresUseCase
import cz.adamec.timotej.snag.structures.fe.app.impl.internal.sync.STRUCTURE_SYNC_ENTITY_TYPE
import cz.adamec.timotej.snag.structures.fe.ports.StructuresDb
import cz.adamec.timotej.snag.sync.fe.app.api.ExecutePullSyncUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

internal class GetStructuresUseCaseImpl(
    private val executePullSyncUseCase: ExecutePullSyncUseCase,
    private val structuresDb: StructuresDb,
    private val applicationScope: ApplicationScope,
) : GetStructuresUseCase {
    override operator fun invoke(projectId: Uuid): Flow<OfflineFirstDataResult<List<AppStructure>>> {
        applicationScope.launch {
            executePullSyncUseCase(
                entityTypeId = STRUCTURE_SYNC_ENTITY_TYPE,
                scopeId = projectId,
            )
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
