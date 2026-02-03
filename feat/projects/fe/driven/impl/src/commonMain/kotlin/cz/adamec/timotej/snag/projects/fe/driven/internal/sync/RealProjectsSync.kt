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

package cz.adamec.timotej.snag.projects.fe.driven.internal.sync

import cz.adamec.timotej.snag.lib.sync.fe.app.EnqueueSyncDeleteUseCase
import cz.adamec.timotej.snag.lib.sync.fe.app.EnqueueSyncSaveUseCase
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsSync
import kotlin.uuid.Uuid

internal class RealProjectsSync(
    private val enqueueSyncSaveUseCase: EnqueueSyncSaveUseCase,
    private val enqueueSyncDeleteUseCase: EnqueueSyncDeleteUseCase,
) : ProjectsSync {
    override suspend fun enqueueProjectSave(projectId: Uuid) {
        enqueueSyncSaveUseCase(PROJECT_SYNC_ENTITY_TYPE, projectId)
    }

    override suspend fun enqueueProjectDelete(projectId: Uuid) {
        enqueueSyncDeleteUseCase(PROJECT_SYNC_ENTITY_TYPE, projectId)
    }
}
