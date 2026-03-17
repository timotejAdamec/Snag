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

import cz.adamec.timotej.snag.core.foundation.common.TimestampProvider
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.projects.fe.app.api.SetProjectClosedUseCase
import cz.adamec.timotej.snag.projects.fe.model.FrontendProject
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsApi
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsDb
import kotlin.uuid.Uuid

class SetProjectClosedUseCaseImpl(
    private val projectsApi: ProjectsApi,
    private val projectsDb: ProjectsDb,
    private val timestampProvider: TimestampProvider,
) : SetProjectClosedUseCase {
    override suspend fun invoke(
        projectId: Uuid,
        isClosed: Boolean,
    ): OnlineDataResult<Unit> {
        val localProject =
            when (val r = getLocalProjectOrFailure(projectId)) {
                is OnlineDataResult.Success -> r.data
                is OnlineDataResult.Failure -> return r
            }
        val updatedProject =
            FrontendProject(
                project =
                    localProject.project.copy(
                        isClosed = isClosed,
                        updatedAt = timestampProvider.getNowTimestamp(),
                    ),
            )
        return when (val result = projectsApi.saveProject(updatedProject)) {
            is OnlineDataResult.Success -> {
                val saved = result.data ?: updatedProject
                projectsDb.saveProject(saved)
                OnlineDataResult.Success(Unit)
            }
            is OnlineDataResult.Failure -> {
                result
            }
        }
    }

    private suspend fun getLocalProjectOrFailure(projectId: Uuid): OnlineDataResult<FrontendProject> =
        when (val dbResult = projectsDb.getProject(projectId)) {
            is OfflineFirstDataResult.Success ->
                dbResult.data?.let { OnlineDataResult.Success(it) }
                    ?: OnlineDataResult.Failure.ProgrammerError(
                        IllegalStateException("Project $projectId not found locally"),
                    )
            is OfflineFirstDataResult.ProgrammerError ->
                OnlineDataResult.Failure.ProgrammerError(dbResult.throwable)
        }
}
