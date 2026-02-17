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

fun NetworkException.log() {
    when (this) {
        is NetworkException.ServerError -> LH.logger.w { "Server error: $message" }
        is NetworkException.ClientError.Unauthorized -> LH.logger.w { "Unauthorized: $message" }
        is NetworkException.ClientError.NotFound -> LH.logger.w { "Not found: $message" }
        is NetworkException.ClientError.OtherClientError -> LH.logger.w { "Other client error: $message" }
        is NetworkException.NetworkUnavailable -> LH.logger.i { "Network unavailable: $cause" }
        is NetworkException.ProgrammerError -> LH.logger.e(throwable = this) { "Programmer error: $message" }
    }
}
