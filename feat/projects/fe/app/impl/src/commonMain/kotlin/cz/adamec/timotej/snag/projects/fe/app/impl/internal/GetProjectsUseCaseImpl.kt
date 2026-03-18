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
import cz.adamec.timotej.snag.projects.app.model.AppProject
import cz.adamec.timotej.snag.projects.fe.app.api.GetProjectsUseCase
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.sync.PROJECT_SYNC_ENTITY_TYPE
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsDb
import cz.adamec.timotej.snag.sync.fe.app.api.ExecutePullSyncUseCase
import cz.adamec.timotej.snag.sync.fe.app.api.model.ExecutePullSyncRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class GetProjectsUseCaseImpl(
    private val executePullSyncUseCase: ExecutePullSyncUseCase,
    private val projectsDb: ProjectsDb,
    private val applicationScope: ApplicationScope,
) : GetProjectsUseCase {
    override operator fun invoke(): Flow<OfflineFirstDataResult<List<AppProject>>> {
        applicationScope.launch {
            executePullSyncUseCase(ExecutePullSyncRequest(entityTypeId = PROJECT_SYNC_ENTITY_TYPE))
        }

        return projectsDb
            .getAllProjectsFlow()
            .onEach {
                LH.logger.log(
                    offlineFirstDataResult = it,
                    additionalInfo = "GetProjectsUseCase, projectsDb.getAllProjectsFlow()",
                )
            }.distinctUntilChanged()
    }
}
