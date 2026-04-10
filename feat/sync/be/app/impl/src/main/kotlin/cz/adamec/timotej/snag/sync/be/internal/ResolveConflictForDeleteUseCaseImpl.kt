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

import cz.adamec.timotej.snag.sync.be.DeleteConflictResult
import cz.adamec.timotej.snag.sync.be.ResolveConflictForDeleteUseCase
import cz.adamec.timotej.snag.sync.be.model.ResolveConflictForDeleteRequest
import cz.adamec.timotej.snag.sync.be.model.SoftDeletable
import cz.adamec.timotej.snag.sync.model.MutableVersioned

internal class ResolveConflictForDeleteUseCaseImpl : ResolveConflictForDeleteUseCase {
    override operator fun <T> invoke(
        request: ResolveConflictForDeleteRequest<T>,
    ): DeleteConflictResult<T> where T : MutableVersioned, T : SoftDeletable {
        val existing = request.existing
        if (existing == null) return DeleteConflictResult.NotFound
        return when {
            existing.deletedAt != null -> DeleteConflictResult.AlreadyDeleted
            existing.updatedAt >= request.deletedAt -> DeleteConflictResult.Rejected(existing)
            else -> DeleteConflictResult.Proceed
        }
    }
}
