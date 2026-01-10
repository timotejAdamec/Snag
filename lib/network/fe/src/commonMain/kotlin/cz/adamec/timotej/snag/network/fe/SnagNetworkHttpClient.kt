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

import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType

class SnagNetworkHttpClient(
    private val httpClient: HttpClient,
) {
    suspend fun get(
        path: String,
        block: HttpRequestBuilder.() -> Unit = {},
    ): HttpResponse {
        return httpClient.get(
            urlString = BASE_URL + path,
        ) {
            block()
        }
    }

    suspend fun post(
        path: String,
        contentType: ContentType = ContentType.Application.Json,
        block: HttpRequestBuilder.() -> Unit = {},
    ): HttpResponse {
        return httpClient.post(
            urlString = BASE_URL + path,
        ) {
            contentType(contentType)
            block()
        }
    }

    suspend fun put(
        path: String,
        contentType: ContentType = ContentType.Application.Json,
        block: HttpRequestBuilder.() -> Unit = {},
    ): HttpResponse {
        return httpClient.put(
            urlString = BASE_URL + path,
        ) {
            contentType(contentType)
            block()
        }
    }

    private companion object {
        const val BASE_URL = "http://localhost:8080"
    }
}
