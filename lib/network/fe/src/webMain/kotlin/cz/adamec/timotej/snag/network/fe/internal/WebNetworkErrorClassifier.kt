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

package cz.adamec.timotej.snag.network.fe.internal

/**
 * On web platforms, the browser's fetch API throws a generic Error("Fail to fetch")
 * when the server is unreachable (e.g. ERR_CONNECTION_REFUSED). Ktor wraps this as
 * a [kotlin.Error], which is not an [kotlinx.io.IOException], so it needs separate handling.
 */
internal class WebNetworkErrorClassifier : DefaultNetworkErrorClassifier() {
    override fun isNetworkUnavailableError(cause: Throwable): Boolean =
        super.isNetworkUnavailableError(cause)
                || cause is Error && cause.message == "Fail to fetch"
}
