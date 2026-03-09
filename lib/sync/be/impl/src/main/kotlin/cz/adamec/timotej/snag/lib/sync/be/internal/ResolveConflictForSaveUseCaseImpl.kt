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
import cz.adamec.timotej.snag.lib.sync.be.ResolveConflictForSaveUseCase
import cz.adamec.timotej.snag.lib.sync.be.SaveConflictResult
import cz.adamec.timotej.snag.lib.sync.be.model.Syncable

internal class ResolveConflictForSaveUseCaseImpl : ResolveConflictForSaveUseCase {
    override operator fun <T : Syncable> invoke(
        existing: T?,
        incoming: T,
    ): SaveConflictResult<T> {
        if (existing == null) return SaveConflictResult.Proceed
        val serverTimestamp =
            maxOf(
                existing.updatedAt,
                existing.deletedAt ?: Timestamp(0),
            )
        return if (serverTimestamp >= incoming.updatedAt) {
            SaveConflictResult.Rejected(existing)
        } else {
            SaveConflictResult.Proceed
        }
    }
}
