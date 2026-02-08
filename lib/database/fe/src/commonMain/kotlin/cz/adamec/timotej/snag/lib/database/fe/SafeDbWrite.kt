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

package cz.adamec.timotej.snag.lib.database.fe

import co.touchlab.kermit.Logger
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

@Suppress("TooGenericExceptionCaught")
suspend fun safeDbWrite(
    ioDispatcher: CoroutineDispatcher,
    logger: Logger,
    errorMessage: String,
    block: suspend () -> Unit,
): OfflineFirstDataResult<Unit> =
    withContext(ioDispatcher) {
        try {
            block()
            OfflineFirstDataResult.Success(
                data = Unit,
            )
        } catch (e: Throwable) {
            logger.e(e) { errorMessage }
            OfflineFirstDataResult.ProgrammerError(
                throwable = e,
            )
        }
    }
