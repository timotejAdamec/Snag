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

package cz.adamec.timotej.snag.lib.store

import cz.adamec.timotej.snag.lib.core.DataResult
import cz.adamec.timotej.snag.network.fe.NetworkException
import cz.adamec.timotej.snag.network.fe.toDataResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform
import org.mobilenativefoundation.store.store5.StoreReadResponse
import org.mobilenativefoundation.store.store5.StoreWriteResponse

fun <T> Flow<StoreReadResponse<T>>.toDataResultFlow(): Flow<DataResult<T>> =
    transform { response ->
        when (response) {
            is StoreReadResponse.Data -> {
                emit(DataResult.Success(response.value))
            }

            is StoreReadResponse.Loading,
            is StoreReadResponse.Initial,
                -> {
                emit(DataResult.Loading)
            }

            is StoreReadResponse.Error.Exception -> {
                emit(response.error.toDataResultFailure())
            }

            is StoreReadResponse.Error.Message -> {
                emit(DataResult.Failure.ProgrammerError(Exception(response.message)))
            }

            is StoreReadResponse.Error.Custom<*> -> {
                emit(DataResult.Failure.ProgrammerError(Exception("Custom error")))
            }

            is StoreReadResponse.NoNewData -> { // Do nothing
            }
        }
    }

fun Throwable.toDataResultFailure() =
    if (this is NetworkException) {
        this.toDataResult()
    } else {
        DataResult.Failure.ProgrammerError(this)
    }

inline fun <reified T> StoreWriteResponse.toDataResult(): DataResult<T> =
    when (this) {
        is StoreWriteResponse.Success.Typed<*> -> {
            val value = this.value as? T
            if (value != null) {
                DataResult.Success(value)
            } else {
                DataResult.Failure.ProgrammerError(
                    RuntimeException("StoreWriteResponse.toDataResult():" +
                            " Couldn't cast to Typed value to ${T::class.simpleName}")
                )
            }
        }

        is StoreWriteResponse.Success.Untyped -> {
            val value = this.value as? T
            if (value != null) {
                DataResult.Success(value)
            } else {
                DataResult.Failure.ProgrammerError(
                    RuntimeException("StoreWriteResponse.toDataResult():" +
                            " Couldn't cast Untyped value to ${T::class.simpleName}")
                )
            }
        }

        is StoreWriteResponse.Error.Exception -> this.error.toDataResultFailure()
        is StoreWriteResponse.Error.Message ->
            DataResult.Failure.ProgrammerError(
                Exception(
                    this.message,
                ),
            )
    }

inline fun <reified T> StoreWriteResponse.toOfflineFirstDataResult(
    localValue: T,
): DataResult<T> {
    val rawResult: DataResult<T> = toDataResult()
    return if (rawResult is DataResult.Failure.NetworkUnavailable) {
        DataResult.Success(
            data = localValue,
            locallyOnly = true,
        )
    } else {
        rawResult
    }
}
