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

import cz.adamec.timotej.snag.projects.fe.app.api.PullProjectChangesUseCase
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.sync.PROJECT_SYNC_ENTITY_TYPE
import cz.adamec.timotej.snag.sync.fe.app.api.ExecutePullSyncUseCase

internal class PullProjectChangesUseCaseImpl(
    private val executePullSyncUseCase: ExecutePullSyncUseCase,
) : PullProjectChangesUseCase {
    override suspend fun invoke() {
        executePullSyncUseCase(entityTypeId = PROJECT_SYNC_ENTITY_TYPE)
    }
}
