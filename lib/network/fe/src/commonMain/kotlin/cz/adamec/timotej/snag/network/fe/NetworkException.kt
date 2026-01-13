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
    override val cause: Throwable,
    override val message: String? = null,
) : RuntimeException(message, cause) {
    /** No internet, Airplane mode, DNS failure, or Timeout */
    class NetworkUnavailable(
        override val cause: Throwable,
    ) : NetworkException(cause)

    /** Server returned 4xx */
    sealed class ClientError(
        override val cause: Throwable,
        override val message: String? = null,
    ) : NetworkException(cause, message) {
        /** Server returned 401 */
        class Unauthorized(
            override val cause: Throwable,
        ) : ClientError(cause)

        /** Server returned 404 */
        class NotFound(
            override val cause: Throwable,
        ) : ClientError(cause)

        class OtherClientError(
            override val message: String,
            override val cause: Throwable,
        ) : ClientError(cause, message)
    }

    /** Server returned 5xx */
    class ServerError(
        override val message: String,
        override val cause: Throwable,
    ) : NetworkException(cause, message)

    /** Fallback for everything else */
    class ProgrammerError(
        override val cause: Throwable,
    ) : NetworkException(cause)
}
