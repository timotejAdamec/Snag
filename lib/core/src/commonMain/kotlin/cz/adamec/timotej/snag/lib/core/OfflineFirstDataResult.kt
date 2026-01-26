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

package cz.adamec.timotej.snag.lib.core

import co.touchlab.kermit.Logger

sealed interface OfflineFirstDataResult<out T> {

    data class Success<T>(
        val data: T,
    ) : OfflineFirstDataResult<T>

    data class ProgrammerError(
        val throwable: Throwable,
    ) : OfflineFirstDataResult<Nothing>
}

inline fun <T, R> OfflineFirstDataResult<T>.map(transform: (T) -> R): OfflineFirstDataResult<R> =
    when (this) {
        is OfflineFirstDataResult.Success -> OfflineFirstDataResult.Success(
            data = transform(data),
        )
        is OfflineFirstDataResult.ProgrammerError -> this
    }

fun <T> Logger.log(
    offlineFirstDataResult: OfflineFirstDataResult<T>,
    additionalInfo: String? = null,
) {
    var message =
        when (offlineFirstDataResult) {
            is OfflineFirstDataResult.Success -> "Successful offline first data result: $offlineFirstDataResult"
            is OfflineFirstDataResult.ProgrammerError -> "Programmer error"
        }
    additionalInfo?.let { message += ", additionalInfo: $it" }
    when (offlineFirstDataResult) {
        is OfflineFirstDataResult.Success -> v(message)
        is OfflineFirstDataResult.ProgrammerError -> e(
            message,
            offlineFirstDataResult.throwable,
        )
    }
}
