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

sealed interface DataResult<out T> {

    data class Success<T>(
        val data: T,
        val locallyOnly: Boolean = false,
    ) : DataResult<T>

    sealed interface Failure : DataResult<Nothing> {
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

    object Loading : DataResult<Nothing> {
        override fun toString(): String = "DataResult.Loading"
    }
}

inline fun <T, R> DataResult<T>.map(transform: (T) -> R): DataResult<R> =
    when (this) {
        is DataResult.Success -> DataResult.Success(transform(data))
        is DataResult.Failure -> this
        DataResult.Loading -> DataResult.Loading
    }

fun <T> Logger.log(
    dataResult: DataResult<T>,
    additionalInfo: String? = null,
) {
    var message =
        when (dataResult) {
            is DataResult.Success -> "Successful data result: $dataResult"
            DataResult.Failure.NetworkUnavailable -> "Network unavailable"
            is DataResult.Failure.ProgrammerError -> "Programmer error"
            is DataResult.Failure.UserMessageError -> "User message error: ${dataResult.message}"
            DataResult.Loading -> "Loading data"
        }
    additionalInfo?.let { message += ", additionalInfo: $it" }
    when (dataResult) {
        is DataResult.Success -> v(message)
        DataResult.Failure.NetworkUnavailable -> w(message)
        is DataResult.Failure.ProgrammerError -> e(message, dataResult.throwable)
        is DataResult.Failure.UserMessageError -> w(message, dataResult.throwable)
        DataResult.Loading -> v(message)
    }
}
