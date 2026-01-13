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
import io.ktor.client.HttpClient
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException

object HttpClientFactory {

    fun createHttpClient(): HttpClient = HttpClient {
        expectSuccess = true

        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }

        HttpResponseValidator {
            handleResponseExceptionWithRequest { cause, _ ->
                when (cause) {
                    is IOException,
                    is HttpRequestTimeoutException,
                    is ConnectTimeoutException,
                    is SocketTimeoutException,
                        -> throw NetworkException.NetworkUnavailable(
                        cause = cause,
                    )

                    is ClientRequestException -> {
                        when (cause.response.status) {
                            HttpStatusCode.Unauthorized -> throw NetworkException.ClientError.Unauthorized(
                                cause = cause,
                            )

                            HttpStatusCode.NotFound -> throw NetworkException.ClientError.NotFound(
                                cause = cause,
                            )

                            else -> throw NetworkException.ClientError.OtherClientError(
                                message = cause.message,
                                cause = cause,
                            )
                        }
                    }

                    is ServerResponseException -> throw NetworkException.ServerError(
                        message = cause.message,
                        cause = cause,
                    )

                    is CancellationException -> throw cause
                    else -> throw NetworkException.ProgrammerError(
                        cause = cause,
                    )
                }
            }
        }
    }
}
