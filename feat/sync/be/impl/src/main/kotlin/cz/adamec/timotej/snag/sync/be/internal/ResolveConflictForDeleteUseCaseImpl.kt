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

package cz.adamec.timotej.snag.sync.be.internal

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.sync.be.DeleteConflictResult
import cz.adamec.timotej.snag.sync.be.ResolveConflictForDeleteUseCase
import cz.adamec.timotej.snag.sync.be.model.Syncable

internal class ResolveConflictForDeleteUseCaseImpl : ResolveConflictForDeleteUseCase {
    override operator fun <T : Syncable> invoke(
        existing: T?,
        deletedAt: Timestamp,
    ): DeleteConflictResult<T> {
        if (existing == null) return DeleteConflictResult.NotFound
        return when {
            existing.deletedAt != null -> DeleteConflictResult.AlreadyDeleted
            existing.updatedAt >= deletedAt -> DeleteConflictResult.Rejected(existing)
            else -> DeleteConflictResult.Proceed
        }
    }
}
