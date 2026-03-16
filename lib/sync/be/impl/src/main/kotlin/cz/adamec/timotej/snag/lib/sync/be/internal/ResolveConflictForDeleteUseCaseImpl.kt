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
import cz.adamec.timotej.snag.lib.sync.be.model.SoftDeletable
import cz.adamec.timotej.snag.lib.sync.model.Versioned

internal class ResolveConflictForDeleteUseCaseImpl : ResolveConflictForDeleteUseCase {
    override operator fun <T> invoke(
        existing: T?,
        deletedAt: Timestamp,
    ): DeleteConflictResult<T> where T : Versioned, T : SoftDeletable {
        if (existing == null) return DeleteConflictResult.NotFound
        return when {
            existing.deletedAt != null -> DeleteConflictResult.AlreadyDeleted
            existing.updatedAt >= deletedAt -> DeleteConflictResult.Rejected(existing)
            else -> DeleteConflictResult.Proceed
        }
    }
}
