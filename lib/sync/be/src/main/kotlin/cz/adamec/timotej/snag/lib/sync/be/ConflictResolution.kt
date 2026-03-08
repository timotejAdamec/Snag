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

package cz.adamec.timotej.snag.lib.sync.be

import cz.adamec.timotej.snag.lib.core.common.Timestamp

sealed interface SaveConflictResult<out T : Syncable> {
    data object Proceed : SaveConflictResult<Nothing>

    data class Rejected<T : Syncable>(val serverVersion: T) : SaveConflictResult<T>
}

sealed interface DeleteConflictResult<out T : Syncable> {
    data object Proceed : DeleteConflictResult<Nothing>

    data object NotFound : DeleteConflictResult<Nothing>

    data object AlreadyDeleted : DeleteConflictResult<Nothing>

    data class Rejected<T : Syncable>(val serverVersion: T) : DeleteConflictResult<T>
}

fun <T : Syncable> resolveConflictForSave(
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

fun <T : Syncable> resolveConflictForDelete(
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
