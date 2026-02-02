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

package cz.adamec.timotej.snag.lib.core.fe

import co.touchlab.kermit.Logger

sealed interface OfflineFirstUpdateDataResult {
    data object Success : OfflineFirstUpdateDataResult

    data object NotFound : OfflineFirstUpdateDataResult

    data class ProgrammerError(
        val throwable: Throwable,
    ) : OfflineFirstUpdateDataResult
}

fun Logger.log(
    offlineFirstUpdateDataResult: OfflineFirstUpdateDataResult,
    additionalInfo: String? = null,
) {
    var message =
        when (offlineFirstUpdateDataResult) {
            is OfflineFirstUpdateDataResult.Success -> "Successful offline first update data result"
            is OfflineFirstUpdateDataResult.NotFound -> "Not found"
            is OfflineFirstUpdateDataResult.ProgrammerError -> "Programmer error"
        }
    additionalInfo?.let { message += ", additionalInfo: $it" }
    when (offlineFirstUpdateDataResult) {
        is OfflineFirstUpdateDataResult.Success -> v(message)
        is OfflineFirstUpdateDataResult.NotFound -> w(message)
        is OfflineFirstUpdateDataResult.ProgrammerError ->
            e(
                throwable = offlineFirstUpdateDataResult.throwable,
                messageString = message,
            )
    }
}
