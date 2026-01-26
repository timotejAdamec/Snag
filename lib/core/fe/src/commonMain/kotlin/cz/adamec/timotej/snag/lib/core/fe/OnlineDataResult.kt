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

sealed interface OnlineDataResult<out T> {

    data class Success<T>(
        val data: T,
    ) : OnlineDataResult<T>

    sealed interface Failure : OnlineDataResult<Nothing> {

        object NetworkUnavailable : Failure {
            override fun toString(): String = "DataResult.NetworkUnavailable"
        }

        data class UserMessageError(
            val throwable: Throwable,
            val message: String,
        ) : Failure

        data class ProgrammerError(
            val throwable: Throwable,
        ) : Failure
    }
}

inline fun <T, R> OnlineDataResult<T>.map(transform: (T) -> R): OnlineDataResult<R> =
    when (this) {
        is OnlineDataResult.Success -> OnlineDataResult.Success(transform(data))
        is OnlineDataResult.Failure -> this
    }

fun <T> Logger.log(
    offlineFirstDataResult: OnlineDataResult<T>,
    additionalInfo: String? = null,
) {
    var message =
        when (offlineFirstDataResult) {
            is OnlineDataResult.Success -> "Successful online data result: $offlineFirstDataResult"
            OnlineDataResult.Failure.NetworkUnavailable -> "Network unavailable"
            is OnlineDataResult.Failure.ProgrammerError -> "Programmer error"
            is OnlineDataResult.Failure.UserMessageError -> "User message error: ${offlineFirstDataResult.message}"
        }
    additionalInfo?.let { message += ", additionalInfo: $it" }
    when (offlineFirstDataResult) {
        is OnlineDataResult.Success -> v(message)
        OnlineDataResult.Failure.NetworkUnavailable -> w(message)
        is OnlineDataResult.Failure.ProgrammerError -> e(message, offlineFirstDataResult.throwable)
        is OnlineDataResult.Failure.UserMessageError -> w(message, offlineFirstDataResult.throwable)
    }
}
