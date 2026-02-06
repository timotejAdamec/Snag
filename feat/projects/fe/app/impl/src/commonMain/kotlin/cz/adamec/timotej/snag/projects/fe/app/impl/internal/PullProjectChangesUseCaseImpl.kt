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

import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.common.TimestampProvider
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.projects.fe.app.api.PullProjectChangesUseCase
import cz.adamec.timotej.snag.projects.fe.ports.ProjectSyncResult
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsApi
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsDb
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsPullSyncCoordinator
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsPullSyncTimestampDataSource
import cz.adamec.timotej.snag.structures.fe.app.api.CascadeDeleteLocalStructuresByProjectIdUseCase

internal class PullProjectChangesUseCaseImpl(
    private val projectsApi: ProjectsApi,
    private val projectsDb: ProjectsDb,
    private val cascadeDeleteLocalStructuresByProjectIdUseCase: CascadeDeleteLocalStructuresByProjectIdUseCase,
    private val projectsPullSyncTimestampDataSource: ProjectsPullSyncTimestampDataSource,
    private val projectsPullSyncCoordinator: ProjectsPullSyncCoordinator,
    private val timestampProvider: TimestampProvider,
) : PullProjectChangesUseCase {
    override suspend operator fun invoke() {
        LH.logger.d { "Starting pull sync for projects." }
        projectsPullSyncCoordinator.withFlushedQueue {
            val since = projectsPullSyncTimestampDataSource.getLastSyncedAt() ?: Timestamp(0)
            val now = timestampProvider.getNowTimestamp()
            LH.logger.d { "Pulling project changes since=$since, now=$now." }

            when (val result = projectsApi.getProjectsModifiedSince(since)) {
                is OnlineDataResult.Failure -> {
                    LH.logger.w { "Error pulling project changes." }
                }
                is OnlineDataResult.Success -> {
                    val changes = result.data
                    LH.logger.d { "Received ${changes.size} project change(s)." }
                    changes.forEach { syncResult ->
                        when (syncResult) {
                            is ProjectSyncResult.Deleted -> {
                                LH.logger.d { "Processing deleted project ${syncResult.id}." }
                                cascadeDeleteLocalStructuresByProjectIdUseCase(syncResult.id)
                                projectsDb.deleteProject(syncResult.id)
                            }
                            is ProjectSyncResult.Updated -> {
                                LH.logger.d { "Processing updated project ${syncResult.project.project.id}." }
                                projectsDb.saveProject(syncResult.project)
                            }
                        }
                    }
                    projectsPullSyncTimestampDataSource.setLastSyncedAt(now)
                    LH.logger.d { "Pull sync for projects completed, updated lastSyncedAt=$now." }
                }
            }
        }
    }
}
