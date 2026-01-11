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

package cz.adamec.timotej.snag.feat.shared.database.fe

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

fun <T> Flow<T>.catchAsDatabaseResult(): Flow<DatabaseResult<T>> {
    return this
        .map<T, DatabaseResult<T>> { DatabaseResult.Success(it) }
        .catch { e -> emit(DatabaseResult.Failure(e)) }
}
