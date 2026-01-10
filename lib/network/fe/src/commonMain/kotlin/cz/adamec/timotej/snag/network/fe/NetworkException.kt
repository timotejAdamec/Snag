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

sealed class NetworkException(
    override val message: String,
    override val cause: Throwable,
) : RuntimeException(message, cause) {

    /** No internet, Airplane mode, DNS failure, or Timeout */
    class NetworkUnavailable(
        override val message: String,
        override val cause: Throwable,
    ) : NetworkException(message, cause)

    /** Server returned 4xx */
    sealed class ClientError(
        override val message: String,
        override val cause: Throwable,
    ) : NetworkException(message, cause) {

        /** Server returned 401 */
        class Unauthorized(
            override val message: String,
            override val cause: Throwable,
        ) : ClientError(message, cause)

        /** Server returned 404 */
        class NotFound(
            override val message: String,
            override val cause: Throwable,
        ) : ClientError(message, cause)

        class OtherClientError(
            override val message: String,
            override val cause: Throwable,
        ) : ClientError(message, cause)
    }

    /** Server returned 5xx */
    class ServerError(
        override val message: String,
        override val cause: Throwable,
    ) : NetworkException(message, cause)

    /** Fallback for everything else */
    class ProgrammerError(
        override val message: String,
        override val cause: Throwable,
    ) : NetworkException(message, cause)
}
