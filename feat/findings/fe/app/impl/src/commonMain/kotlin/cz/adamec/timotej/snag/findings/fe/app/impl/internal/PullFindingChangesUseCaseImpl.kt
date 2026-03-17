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

package cz.adamec.timotej.snag.findings.fe.app.impl.internal

import cz.adamec.timotej.snag.findings.fe.app.api.PullFindingChangesUseCase
import cz.adamec.timotej.snag.findings.fe.app.impl.internal.sync.FINDING_SYNC_ENTITY_TYPE
import cz.adamec.timotej.snag.sync.fe.app.api.ExecutePullSyncUseCase
import kotlin.uuid.Uuid

internal class PullFindingChangesUseCaseImpl(
    private val executePullSyncUseCase: ExecutePullSyncUseCase,
) : PullFindingChangesUseCase {
    override suspend fun invoke(structureId: Uuid) {
        executePullSyncUseCase(
            entityTypeId = FINDING_SYNC_ENTITY_TYPE,
            scopeId = structureId.toString(),
        )
    }
}
