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

import cz.adamec.timotej.snag.lib.core.common.ApplicationScope
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.core.fe.log
import cz.adamec.timotej.snag.projects.fe.app.api.GetProjectsUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.PullProjectChangesUseCase
import cz.adamec.timotej.snag.projects.fe.model.FrontendProject
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsDb
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class GetProjectsUseCaseImpl(
    private val pullProjectChangesUseCase: PullProjectChangesUseCase,
    private val projectsDb: ProjectsDb,
    private val applicationScope: ApplicationScope,
) : GetProjectsUseCase {
    override operator fun invoke(): Flow<OfflineFirstDataResult<List<FrontendProject>>> {
        applicationScope.launch {
            pullProjectChangesUseCase()
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
