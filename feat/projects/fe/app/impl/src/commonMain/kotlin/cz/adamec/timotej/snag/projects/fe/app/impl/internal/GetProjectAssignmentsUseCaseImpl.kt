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
import cz.adamec.timotej.snag.core.network.fe.log
import cz.adamec.timotej.snag.projects.fe.app.api.GetProjectAssignmentsUseCase
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.sync.PROJECT_ASSIGNMENT_SYNC_ENTITY_TYPE
import cz.adamec.timotej.snag.projects.fe.ports.ProjectAssignmentsDb
import cz.adamec.timotej.snag.sync.fe.app.api.ExecutePullSyncUseCase
import cz.adamec.timotej.snag.sync.fe.app.api.model.ExecutePullSyncRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

internal class GetProjectAssignmentsUseCaseImpl(
    private val executePullSyncUseCase: ExecutePullSyncUseCase,
    private val projectAssignmentsDb: ProjectAssignmentsDb,
    private val applicationScope: ApplicationScope,
) : GetProjectAssignmentsUseCase {
    override operator fun invoke(projectId: Uuid): Flow<OfflineFirstDataResult<Set<Uuid>>> {
        applicationScope.launch {
            executePullSyncUseCase(
                ExecutePullSyncRequest(
                    entityTypeId = PROJECT_ASSIGNMENT_SYNC_ENTITY_TYPE,
                    scopeId = projectId,
                ),
            )
        }

        return projectAssignmentsDb
            .getAssignedUserIdsFlow(projectId)
            .onEach {
                LH.logger.log(
                    offlineFirstDataResult = it,
                    additionalInfo =
                        "GetProjectAssignmentsUseCase, projectAssignmentsDb.getAssignedUserIdsFlow($projectId)",
                )
            }.distinctUntilChanged()
    }
}
