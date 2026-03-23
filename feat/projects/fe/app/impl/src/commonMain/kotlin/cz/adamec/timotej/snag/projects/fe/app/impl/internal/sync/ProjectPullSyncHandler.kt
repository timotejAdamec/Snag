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

package cz.adamec.timotej.snag.projects.fe.app.impl.internal.sync

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.foundation.common.TimestampProvider
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.feat.inspections.fe.app.api.CascadeDeleteLocalInspectionsByProjectIdUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.CascadeDeleteLocalAssignmentsByProjectIdUseCase
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.LH
import cz.adamec.timotej.snag.projects.fe.ports.ProjectSyncResult
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsApi
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsDb
import cz.adamec.timotej.snag.structures.fe.app.api.CascadeDeleteLocalStructuresByProjectIdUseCase
import cz.adamec.timotej.snag.sync.fe.app.api.GetLastPullSyncedAtTimestampUseCase
import cz.adamec.timotej.snag.sync.fe.app.api.SetLastPullSyncedAtTimestampUseCase
import cz.adamec.timotej.snag.sync.fe.app.api.handler.DbApiPullSyncHandler
import kotlin.uuid.Uuid

internal class ProjectPullSyncHandler(
    private val projectsApi: ProjectsApi,
    private val projectsDb: ProjectsDb,
    private val cascadeDeleteLocalStructuresByProjectIdUseCase: CascadeDeleteLocalStructuresByProjectIdUseCase,
    private val cascadeDeleteLocalInspectionsByProjectIdUseCase: CascadeDeleteLocalInspectionsByProjectIdUseCase,
    private val cascadeDeleteLocalAssignmentsByProjectIdUseCase: CascadeDeleteLocalAssignmentsByProjectIdUseCase,
    getLastPullSyncedAtTimestampUseCase: GetLastPullSyncedAtTimestampUseCase,
    setLastPullSyncedAtTimestampUseCase: SetLastPullSyncedAtTimestampUseCase,
    timestampProvider: TimestampProvider,
) : DbApiPullSyncHandler<ProjectSyncResult>(
        logger = LH.logger,
        timestampProvider = timestampProvider,
        getLastPullSyncedAtTimestampUseCase = getLastPullSyncedAtTimestampUseCase,
        setLastPullSyncedAtTimestampUseCase = setLastPullSyncedAtTimestampUseCase,
    ) {
    override val entityTypeId: String = PROJECT_SYNC_ENTITY_TYPE
    override val entityName: String = "project"

    override suspend fun fetchChangesFromApi(
        scopeId: Uuid?,
        since: Timestamp,
    ): OnlineDataResult<List<ProjectSyncResult>> = projectsApi.getProjectsModifiedSince(since)

    override suspend fun applyChange(change: ProjectSyncResult) {
        when (change) {
            is ProjectSyncResult.Deleted -> {
                LH.logger.d { "Processing deleted project ${change.id}." }
                cascadeDeleteLocalStructuresByProjectIdUseCase(change.id)
                cascadeDeleteLocalInspectionsByProjectIdUseCase(change.id)
                cascadeDeleteLocalAssignmentsByProjectIdUseCase(change.id)
                projectsDb.deleteProject(change.id)
            }
            is ProjectSyncResult.Updated -> {
                LH.logger.d { "Processing updated project ${change.project.id}." }
                projectsDb.saveProject(change.project)
            }
        }
    }
}
