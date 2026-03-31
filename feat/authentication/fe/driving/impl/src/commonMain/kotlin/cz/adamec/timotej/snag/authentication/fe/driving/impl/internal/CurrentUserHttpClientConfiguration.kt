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

import cz.adamec.timotej.snag.authentication.fe.app.api.GetAccessTokenUseCase
import cz.adamec.timotej.snag.configuration.common.CommonConfiguration
import cz.adamec.timotej.snag.network.fe.HttpClientConfiguration
import cz.adamec.timotej.snag.routing.common.USER_ID_HEADER
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer

@Suppress("LabeledExpression")
internal class CurrentUserHttpClientConfiguration(
    private val getAccessTokenUseCase: GetAccessTokenUseCase,
) : HttpClientConfiguration {
    override fun HttpClientConfig<*>.setup() {
        if (CommonConfiguration.mockAuth) {
            installMockAuth()
        } else {
            installBearerAuth()
        }
    }

    private fun HttpClientConfig<*>.installMockAuth() {
        install(
            createClientPlugin("MockAuthPlugin") {
                onRequest { request, _ ->
                    val token = getAccessTokenUseCase() ?: return@onRequest
                    request.headers.append(USER_ID_HEADER, token)
                }
            },
        )
    }

    private fun HttpClientConfig<*>.installBearerAuth() {
        install(Auth) {
            bearer {
                loadTokens {
                    getAccessTokenUseCase()
                        ?.let { BearerTokens(accessToken = it, refreshToken = "") }
                }
                sendWithoutRequest { true }
            }
        }
    }
}
