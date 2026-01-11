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

import cz.adamec.timotej.snag.network.fe.NetworkResult.Failure
import cz.adamec.timotej.snag.network.fe.NetworkResult.Success

class CallNetworkWithResult {
    inline operator fun <T> invoke(block: () -> T): NetworkResult<T> {
        return try {
            Success(block())
        } catch (e: NetworkException) {
            when (e) {
                is NetworkException.NetworkUnavailable -> Failure.Connectivity(e)
                is NetworkException.ClientError.Unauthorized -> Failure.Unauthorized(e)
                is NetworkException.ClientError.NotFound -> Failure.NotFound(e)
                is NetworkException.ClientError.OtherClientError -> Failure.UserError(e, e.message)
                is NetworkException.ServerError -> Failure.ServerError(e)
                is NetworkException.ProgrammerError -> Failure.Programmer(e)
            }
        }
    }
}
