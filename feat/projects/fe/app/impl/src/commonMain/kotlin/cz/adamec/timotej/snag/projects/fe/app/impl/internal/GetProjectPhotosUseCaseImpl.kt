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

package cz.adamec.timotej.snag.projects.fe.app.impl.internal

import cz.adamec.timotej.snag.core.foundation.common.ApplicationScope
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.projects.app.model.AppProjectPhoto
import cz.adamec.timotej.snag.projects.fe.app.api.GetProjectPhotosUseCase
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.sync.PROJECT_PHOTO_SYNC_ENTITY_TYPE
import cz.adamec.timotej.snag.projects.fe.ports.ProjectPhotosDb
import cz.adamec.timotej.snag.sync.fe.app.api.ExecutePullSyncUseCase
import cz.adamec.timotej.snag.sync.fe.app.api.model.ExecutePullSyncRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

internal class GetProjectPhotosUseCaseImpl(
    private val executePullSyncUseCase: ExecutePullSyncUseCase,
    private val projectPhotosDb: ProjectPhotosDb,
    private val applicationScope: ApplicationScope,
) : GetProjectPhotosUseCase {
    override operator fun invoke(projectId: Uuid): Flow<OfflineFirstDataResult<List<AppProjectPhoto>>> {
        applicationScope.launch {
            executePullSyncUseCase(
                ExecutePullSyncRequest(
                    entityTypeId = PROJECT_PHOTO_SYNC_ENTITY_TYPE,
                    scopeId = projectId,
                ),
            )
        }
        return projectPhotosDb
            .getPhotosFlow(projectId)
            .distinctUntilChanged()
    }
}
