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

package cz.adamec.timotej.snag.network.be.impl.internal

import cz.adamec.timotej.snag.configuration.be.BackendRunConfig
import cz.adamec.timotej.snag.network.be.KtorServerConfiguration
import cz.adamec.timotej.snag.routing.common.USER_ID_HEADER
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.cors.routing.CORS

internal class CorsConfiguration : KtorServerConfiguration {

    override val priority: Int get() = -1

    override fun Application.setup() {
        install(CORS) {
            for (host in BackendRunConfig.corsAllowedHosts) {
                if ("://" in host) {
                    val (scheme, rest) = host.split("://", limit = 2)
                    allowHost(rest, schemes = listOf(scheme))
                } else {
                    allowHost(host)
                }
            }
            allowMethod(HttpMethod.Post)
            allowMethod(HttpMethod.Put)
            allowMethod(HttpMethod.Delete)
            allowMethod(HttpMethod.Options)
            allowHeader(HttpHeaders.ContentType)
            allowHeader(HttpHeaders.Authorization)
            if (BackendRunConfig.mockAuth) {
                allowHeader(USER_ID_HEADER)
            }
        }
    }
}
