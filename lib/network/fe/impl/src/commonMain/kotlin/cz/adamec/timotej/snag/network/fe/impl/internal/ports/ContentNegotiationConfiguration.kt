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

package cz.adamec.timotej.snag.network.fe.impl.internal.ports

import cz.adamec.timotej.snag.network.common.configureJson
import cz.adamec.timotej.snag.network.fe.ports.KtorClientConfiguration
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation

internal class ContentNegotiationConfiguration : KtorClientConfiguration {
    override fun HttpClientConfig<*>.setup() {
        install(ContentNegotiation) {
            configureJson()
        }
    }
}
