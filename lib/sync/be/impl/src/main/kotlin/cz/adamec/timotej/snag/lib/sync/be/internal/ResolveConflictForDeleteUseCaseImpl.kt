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

package cz.adamec.timotej.snag.lib.sync.be.internal

import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.sync.be.DeleteConflictResult
import cz.adamec.timotej.snag.lib.sync.be.ResolveConflictForDeleteUseCase
import cz.adamec.timotej.snag.lib.sync.be.Syncable

internal class ResolveConflictForDeleteUseCaseImpl : ResolveConflictForDeleteUseCase {
    override operator fun <T : Syncable> invoke(
        existing: T?,
        deletedAt: Timestamp,
    ): DeleteConflictResult<T> {
        if (existing == null) return DeleteConflictResult.NotFound
        if (existing.deletedAt != null) return DeleteConflictResult.AlreadyDeleted
        return if (existing.updatedAt >= deletedAt) {
            DeleteConflictResult.Rejected(existing)
        } else {
            DeleteConflictResult.Proceed
        }
    }
}
