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

import cz.adamec.timotej.snag.sync.be.model.ResolveConflictForSaveRequest
import cz.adamec.timotej.snag.sync.be.model.SoftDeletable
import cz.adamec.timotej.snag.sync.model.MutableVersioned

sealed interface SaveConflictResult<out T> where T : MutableVersioned, T : SoftDeletable {
    data object Proceed : SaveConflictResult<Nothing>

    data class Rejected<T>(
        val serverVersion: T,
    ) : SaveConflictResult<T> where T : MutableVersioned, T : SoftDeletable
}

interface ResolveConflictForSaveUseCase {
    operator fun <T> invoke(request: ResolveConflictForSaveRequest<T>): SaveConflictResult<T> where T : MutableVersioned, T : SoftDeletable
}
