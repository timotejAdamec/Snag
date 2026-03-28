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

package cz.adamec.timotej.snag.authentication.fe.driving.impl.internal

import cz.adamec.timotej.snag.configuration.common.CommonConfiguration
import cz.adamec.timotej.snag.network.fe.HttpClientConfiguration
import cz.adamec.timotej.snag.routing.common.AUTHORIZATION_HEADER
import cz.adamec.timotej.snag.routing.common.USER_ID_HEADER
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.api.createClientPlugin

@Suppress("LabeledExpression")
internal class CallCurrentUserConfiguration(
    private val authTokenProvider: AuthTokenProvider,
) : HttpClientConfiguration {
    override fun HttpClientConfig<*>.setup() {
        install(
            createClientPlugin("AuthHeaderPlugin") {
                onRequest { request, _ ->
                    val token = authTokenProvider.getAccessToken() ?: return@onRequest
                    if (CommonConfiguration.mockAuth) {
                        request.headers.append(USER_ID_HEADER, token)
                    } else {
                        request.headers.append(AUTHORIZATION_HEADER, "Bearer $token")
                    }
                }
            },
        )
    }
}
