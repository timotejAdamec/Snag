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
import cz.adamec.timotej.snag.core.foundation.common.UuidProvider
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.core.network.fe.log
import cz.adamec.timotej.snag.core.network.fe.map
import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.projects.fe.app.api.SaveProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.model.SaveProjectRequest
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.sync.PROJECT_SYNC_ENTITY_TYPE
import cz.adamec.timotej.snag.projects.fe.model.FrontendProject
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsDb
import cz.adamec.timotej.snag.sync.fe.app.api.EnqueueSyncSaveUseCase
import kotlin.uuid.Uuid

class SaveProjectUseCaseImpl(
    private val projectsDb: ProjectsDb,
    private val enqueueSyncSaveUseCase: EnqueueSyncSaveUseCase,
    private val uuidProvider: UuidProvider,
    private val timestampProvider: TimestampProvider,
) : SaveProjectUseCase {
    override suspend operator fun invoke(request: SaveProjectRequest): OfflineFirstDataResult<Uuid> {
        val project =
            FrontendProject(
                project =
                    Project(
                        id = request.id ?: uuidProvider.getUuid(),
                        name = request.name,
                        address = request.address,
                        clientId = request.clientId,
                        updatedAt = timestampProvider.getNowTimestamp(),
                    ),
            )

        return projectsDb
            .saveProject(project)
            .also {
                logger.log(
                    offlineFirstDataResult = it,
                    additionalInfo = "SaveProjectUseCase, projectsDb.saveProject($project)",
                )
                if (it is OfflineFirstDataResult.Success) {
                    enqueueSyncSaveUseCase(
                        entityTypeId = PROJECT_SYNC_ENTITY_TYPE,
                        entityId = project.project.id,
                    )
                }
            }.map {
                project.project.id
            }
    }
}
