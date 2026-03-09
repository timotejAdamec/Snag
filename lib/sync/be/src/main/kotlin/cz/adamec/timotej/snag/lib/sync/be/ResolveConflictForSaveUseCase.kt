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

interface ResolveConflictForSaveUseCase {
    operator fun <T : Syncable> invoke(
        existing: T?,
        incoming: T,
    ): SaveConflictResult<T>
}
