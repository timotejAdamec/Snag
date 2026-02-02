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

import cz.adamec.timotej.snag.network.fe.NetworkException
import cz.adamec.timotej.snag.server.api.configureJson
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.HttpStatusCode
import kotlin.coroutines.cancellation.CancellationException
import co.touchlab.kermit.Logger as KermitLogger
import io.ktor.client.plugins.logging.Logger as KtorLogger

internal class HttpClientFactory(
    private val networkErrorClassifier: NetworkErrorClassifier,
) {
    internal fun createHttpClient(): HttpClient =
        HttpClient {
            expectSuccess = true

            install(Logging) {
                logger =
                    object : KtorLogger {
                        override fun log(message: String) {
                            KermitLogger.withTag("HTTP Client").v(message)
                        }
                    }
                level = LogLevel.HEADERS
            }

            install(ContentNegotiation) {
                configureJson()
            }

            HttpResponseValidator {
                handleResponseExceptionWithRequest { cause, _ ->
                    when {
                        cause is CancellationException -> throw cause

                        networkErrorClassifier.isNetworkUnavailableError(cause) ->
                            throw NetworkException.NetworkUnavailable(cause = cause)

                        cause is ClientRequestException ->
                            when (cause.response.status) {
                                HttpStatusCode.Unauthorized -> throw NetworkException.ClientError.Unauthorized(
                                    message = cause.message,
                                    cause = cause,
                                )

                                HttpStatusCode.NotFound -> throw NetworkException.ClientError.NotFound(
                                    message = cause.message,
                                    cause = cause,
                                )

                                else -> throw NetworkException.ClientError.OtherClientError(
                                    message = cause.message,
                                    cause = cause,
                                )
                            }

                        cause is ServerResponseException -> throw NetworkException.ServerError(
                            message = cause.message,
                            cause = cause,
                        )

                        else -> throw NetworkException.ProgrammerError(cause = cause)
                    }
                }
            }
        }
}
