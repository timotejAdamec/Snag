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

package cz.adamec.timotej.snag.feat.inspections.fe.app.impl.internal

import cz.adamec.timotej.snag.feat.inspections.fe.app.api.PullInspectionChangesUseCase
import cz.adamec.timotej.snag.feat.inspections.fe.app.impl.internal.sync.INSPECTION_SYNC_ENTITY_TYPE
import cz.adamec.timotej.snag.sync.fe.app.api.ExecutePullSyncUseCase
import kotlin.uuid.Uuid

internal class PullInspectionChangesUseCaseImpl(
    private val executePullSyncUseCase: ExecutePullSyncUseCase,
) : PullInspectionChangesUseCase {
    override suspend fun invoke(projectId: Uuid) {
        executePullSyncUseCase(
            entityTypeId = INSPECTION_SYNC_ENTITY_TYPE,
            scopeId = projectId.toString(),
        )
    }
}
