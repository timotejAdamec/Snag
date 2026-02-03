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

package cz.adamec.timotej.snag.network.fe

import co.touchlab.kermit.Logger
import cz.adamec.timotej.snag.lib.core.common.runCatchingCancellable
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult

suspend fun <T> safeApiCall(
    logger: Logger,
    errorContext: String,
    block: suspend () -> T,
): OnlineDataResult<T> =
    runCatchingCancellable {
        block()
    }.fold(
        onSuccess = {
            OnlineDataResult.Success(it)
        },
        onFailure = { e ->
            if (e is NetworkException) {
                e.log()
                e.toOnlineDataResult()
            } else {
                logger.e { errorContext }
                OnlineDataResult.Failure.ProgrammerError(
                    throwable = e,
                )
            }
        },
    )
