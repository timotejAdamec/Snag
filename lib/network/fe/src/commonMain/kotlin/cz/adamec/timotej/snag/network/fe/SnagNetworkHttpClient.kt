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

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType

interface SnagNetworkHttpClient {
    suspend fun get(
        path: String,
        block: HttpRequestBuilder.() -> Unit = {},
    ): HttpResponse

    suspend fun put(
        path: String,
        contentType: ContentType = ContentType.Application.Json,
        block: HttpRequestBuilder.() -> Unit = {},
    ): HttpResponse

    suspend fun delete(
        path: String,
        contentType: ContentType = ContentType.Application.Json,
        block: HttpRequestBuilder.() -> Unit = {},
    ): HttpResponse
}
