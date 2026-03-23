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

import cz.adamec.timotej.snag.sync.be.model.ResolveConflictForDeleteRequest
import cz.adamec.timotej.snag.sync.be.model.SoftDeletable
import cz.adamec.timotej.snag.sync.model.MutableVersioned

sealed interface DeleteConflictResult<out T> where T : MutableVersioned, T : SoftDeletable {
    data object Proceed : DeleteConflictResult<Nothing>

    data object NotFound : DeleteConflictResult<Nothing>

    data object AlreadyDeleted : DeleteConflictResult<Nothing>

    data class Rejected<T>(
        val serverVersion: T,
    ) : DeleteConflictResult<T> where T : MutableVersioned, T : SoftDeletable
}

interface ResolveConflictForDeleteUseCase {
    operator fun <T> invoke(request: ResolveConflictForDeleteRequest<T>): DeleteConflictResult<T>
        where T : MutableVersioned, T : SoftDeletable
}
