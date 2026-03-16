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

import cz.adamec.timotej.snag.lib.sync.be.model.ResolveConflictForSaveRequest
import cz.adamec.timotej.snag.lib.sync.be.model.Syncable

sealed interface SaveConflictResult<out T : Syncable> {
    data object Proceed : SaveConflictResult<Nothing>

    data class Rejected<T : Syncable>(
        val serverVersion: T,
    ) : SaveConflictResult<T>
}

interface ResolveConflictForSaveUseCase {
    operator fun <T : Syncable> invoke(request: ResolveConflictForSaveRequest<T>): SaveConflictResult<T>
}
