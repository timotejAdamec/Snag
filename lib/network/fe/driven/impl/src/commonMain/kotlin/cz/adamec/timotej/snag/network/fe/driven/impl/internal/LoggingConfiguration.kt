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

package cz.adamec.timotej.snag.network.fe.driven.impl.internal

import cz.adamec.timotej.snag.network.fe.ports.HttpClientConfiguration
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import co.touchlab.kermit.Logger as KermitLogger
import io.ktor.client.plugins.logging.Logger as KtorLogger

internal class LoggingConfiguration : HttpClientConfiguration {
    override fun HttpClientConfig<*>.setup() {
        install(Logging) {
            logger =
                object : KtorLogger {
                    override fun log(message: String) {
                        KermitLogger.withTag("HTTP Client").v(message)
                    }
                }
            level = LogLevel.BODY
        }
    }
}
