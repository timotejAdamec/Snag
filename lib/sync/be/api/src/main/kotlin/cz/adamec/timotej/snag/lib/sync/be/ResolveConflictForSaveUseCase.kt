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

import cz.adamec.timotej.snag.lib.sync.be.model.SoftDeletable
import cz.adamec.timotej.snag.lib.sync.model.Versioned

sealed interface SaveConflictResult<out T> where T : Versioned, T : SoftDeletable {
    data object Proceed : SaveConflictResult<Nothing>

    data class Rejected<T>(
        val serverVersion: T,
    ) : SaveConflictResult<T> where T : Versioned, T : SoftDeletable
}

interface ResolveConflictForSaveUseCase {
    operator fun <T> invoke(
        existing: T?,
        incoming: T,
    ): SaveConflictResult<T> where T : Versioned, T : SoftDeletable
}
