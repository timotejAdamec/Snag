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

import cz.adamec.timotej.snag.lib.core.DataResult

fun NetworkException.toDataResult() =
    when (this) {
        is NetworkException.NetworkUnavailable ->
            DataResult.Failure.NetworkUnavailable

        is NetworkException.ClientError ->
            DataResult.Failure.UserMessageError(
                throwable = this,
                message = this.message ?: "Invalid request",
            )

        is NetworkException.ServerError ->
            DataResult.Failure.UserMessageError(
                throwable = this,
                message = "Problems on server side.",
            )

        is NetworkException.ProgrammerError ->
            DataResult.Failure.ProgrammerError(this)
    }
