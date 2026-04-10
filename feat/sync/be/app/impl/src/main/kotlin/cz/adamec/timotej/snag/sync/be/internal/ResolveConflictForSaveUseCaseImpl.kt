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
import cz.adamec.timotej.snag.sync.be.ResolveConflictForSaveUseCase
import cz.adamec.timotej.snag.sync.be.SaveConflictResult
import cz.adamec.timotej.snag.sync.be.model.ResolveConflictForSaveRequest
import cz.adamec.timotej.snag.sync.be.model.SoftDeletable
import cz.adamec.timotej.snag.sync.model.MutableVersioned

internal class ResolveConflictForSaveUseCaseImpl : ResolveConflictForSaveUseCase {
    override operator fun <T> invoke(
        request: ResolveConflictForSaveRequest<T>,
    ): SaveConflictResult<T> where T : MutableVersioned, T : SoftDeletable {
        val existing = request.existing
        if (existing == null) return SaveConflictResult.Proceed
        val serverTimestamp =
            maxOf(
                existing.updatedAt,
                existing.deletedAt ?: Timestamp(0),
            )
        return if (serverTimestamp >= request.incoming.updatedAt) {
            SaveConflictResult.Rejected(existing)
        } else {
            SaveConflictResult.Proceed
        }
    }
}
