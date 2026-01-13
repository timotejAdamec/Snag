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

sealed interface DataResult<out T> {
    data class Success<T>(val data: T) : DataResult<T>
    sealed interface Failure : DataResult<Nothing> {

        object NetworkUnavailable : Failure

        data class UserMessageError(
            val throwable: Throwable,
            val message: String,
        ) : Failure

        data class ProgrammerError(
            val throwable: Throwable,
        ) : Failure
    }

    object Loading : DataResult<Nothing>
}
