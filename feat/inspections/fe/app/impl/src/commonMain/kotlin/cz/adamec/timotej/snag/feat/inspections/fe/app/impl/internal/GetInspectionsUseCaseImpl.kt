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

package cz.adamec.timotej.snag.feat.inspections.fe.app.impl.internal

import cz.adamec.timotej.snag.core.foundation.common.ApplicationScope
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.core.network.fe.log
import cz.adamec.timotej.snag.feat.inspections.app.model.AppInspection
import cz.adamec.timotej.snag.feat.inspections.fe.app.api.GetInspectionsUseCase
import cz.adamec.timotej.snag.feat.inspections.fe.app.impl.internal.sync.INSPECTION_SYNC_ENTITY_TYPE
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsDb
import cz.adamec.timotej.snag.sync.fe.app.api.ExecutePullSyncUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

internal class GetInspectionsUseCaseImpl(
    private val executePullSyncUseCase: ExecutePullSyncUseCase,
    private val inspectionsDb: InspectionsDb,
    private val applicationScope: ApplicationScope,
) : GetInspectionsUseCase {
    override operator fun invoke(projectId: Uuid): Flow<OfflineFirstDataResult<List<AppInspection>>> {
        applicationScope.launch {
            executePullSyncUseCase(
                entityTypeId = INSPECTION_SYNC_ENTITY_TYPE,
                scopeId = projectId,
            )
        }

        return inspectionsDb
            .getInspectionsFlow(projectId)
            .onEach {
                LH.logger.log(
                    offlineFirstDataResult = it,
                    additionalInfo = "GetInspectionsUseCase, inspectionsDb.getInspectionsFlow($projectId)",
                )
            }.distinctUntilChanged()
    }
}
