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

package cz.adamec.timotej.snag.clients.fe.app.impl.internal

import cz.adamec.timotej.snag.clients.fe.app.api.PullClientChangesUseCase
import cz.adamec.timotej.snag.clients.fe.app.impl.internal.sync.CLIENT_SYNC_ENTITY_TYPE
import cz.adamec.timotej.snag.sync.fe.app.api.ExecutePullSyncUseCase

internal class PullClientChangesUseCaseImpl(
    private val executePullSyncUseCase: ExecutePullSyncUseCase,
) : PullClientChangesUseCase {
    override suspend fun invoke() {
        executePullSyncUseCase(entityTypeId = CLIENT_SYNC_ENTITY_TYPE)
    }
}
