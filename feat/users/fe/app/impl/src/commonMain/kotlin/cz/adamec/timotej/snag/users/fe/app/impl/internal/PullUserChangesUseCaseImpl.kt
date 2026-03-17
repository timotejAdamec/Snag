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

package cz.adamec.timotej.snag.users.fe.app.impl.internal

import cz.adamec.timotej.snag.sync.fe.app.api.ExecutePullSyncUseCase
import cz.adamec.timotej.snag.users.fe.app.api.PullUserChangesUseCase
import cz.adamec.timotej.snag.users.fe.app.impl.internal.sync.USER_SYNC_ENTITY_TYPE

internal class PullUserChangesUseCaseImpl(
    private val executePullSyncUseCase: ExecutePullSyncUseCase,
) : PullUserChangesUseCase {
    override suspend fun invoke() {
        executePullSyncUseCase(entityTypeId = USER_SYNC_ENTITY_TYPE)
    }
}
