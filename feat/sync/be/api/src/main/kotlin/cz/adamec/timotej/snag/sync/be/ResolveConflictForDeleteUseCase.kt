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

package cz.adamec.timotej.snag.sync.be

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.sync.be.model.Syncable

sealed interface DeleteConflictResult<out T : Syncable> {
    data object Proceed : DeleteConflictResult<Nothing>

    data object NotFound : DeleteConflictResult<Nothing>

    data object AlreadyDeleted : DeleteConflictResult<Nothing>

    data class Rejected<T : Syncable>(
        val serverVersion: T,
    ) : DeleteConflictResult<T>
}

interface ResolveConflictForDeleteUseCase {
    operator fun <T : Syncable> invoke(
        existing: T?,
        deletedAt: Timestamp,
    ): DeleteConflictResult<T>
}
