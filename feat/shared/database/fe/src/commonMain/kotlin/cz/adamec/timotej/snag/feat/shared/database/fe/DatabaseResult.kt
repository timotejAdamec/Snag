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

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

sealed interface DatabaseResult<out T> {
    data class Success<T>(val data: T) : DatabaseResult<T>
    data class Failure(val throwable: Throwable?) : DatabaseResult<Nothing>

    fun getOrNull(): T? = (this as? Success)?.data
    fun getOrThrow(): T = (this as? Success)?.data ?: throw throwableOrNull() ?: Exception("No throwable provided")
    fun throwableOrNull(): Throwable? = (this as? Failure)?.throwable

}

inline fun <T, R> DatabaseResult<T>.map(transform: (T) -> R): DatabaseResult<R> {
    return when (this) {
        is DatabaseResult.Success -> DatabaseResult.Success(transform(data))
        is DatabaseResult.Failure -> this
    }
}

@OptIn(ExperimentalContracts::class)
inline fun <T> DatabaseResult<T>.onSuccess(action: (value: T) -> Unit): DatabaseResult<T> {
    contract {
        callsInPlace(action, InvocationKind.AT_MOST_ONCE)
    }
    if (this is DatabaseResult.Success) {
        action(data)
    }
    return this
}

@OptIn(ExperimentalContracts::class)
inline fun <T> DatabaseResult<T>.onFailure(action: (throwable: Throwable?) -> Unit): DatabaseResult<T> {
    contract {
        callsInPlace(action, InvocationKind.AT_MOST_ONCE)
    }
    if (this is DatabaseResult.Failure) {
        action(throwable)
    }
    return this
}
