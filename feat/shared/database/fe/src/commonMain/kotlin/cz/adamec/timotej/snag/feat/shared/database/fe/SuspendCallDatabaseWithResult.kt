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

import cz.adamec.timotej.snag.feat.shared.database.fe.internal.CallDatabaseWithResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class SuspendCallDatabaseWithResult(
    private val ioDispatcher: CoroutineDispatcher,
) {
    private val callDatabaseWithResult
        get() = CallDatabaseWithResult()

    suspend operator fun <T> invoke(block: suspend () -> T): DatabaseResult<T> {
        return withContext(ioDispatcher) {
            callDatabaseWithResult {
                block()
            }
        }
    }
}
