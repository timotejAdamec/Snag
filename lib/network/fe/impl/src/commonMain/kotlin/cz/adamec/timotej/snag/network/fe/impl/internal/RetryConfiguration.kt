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

package cz.adamec.timotej.snag.network.fe.impl.internal

import cz.adamec.timotej.snag.network.fe.HttpClientConfiguration
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpRequestRetry

internal class RetryConfiguration : HttpClientConfiguration {
    override fun HttpClientConfig<*>.setup() {
        install(HttpRequestRetry) {
            retryOnServerErrors(maxRetries = 3)
            retryOnException(maxRetries = 3, retryOnTimeout = true)
            exponentialDelay()
        }
    }
}
