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

import cz.adamec.timotej.snag.network.fe.SnagNetworkHttpClient
import cz.adamec.timotej.snag.server.api.Host
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType

internal class SnagNetworkHttpClientImpl(
    private val httpClient: HttpClient,
    private val localHostUrlFactory: LocalHostUrlFactory,
) : SnagNetworkHttpClient {

    override suspend fun get(
        path: String,
        block: HttpRequestBuilder.() -> Unit,
    ): HttpResponse =
        httpClient.get(
            urlString = localHostUrlFactory.createUrl() + path,
        ) {
            block()
        }

    override suspend fun put(
        path: String,
        contentType: ContentType,
        block: HttpRequestBuilder.() -> Unit,
    ): HttpResponse =
        httpClient.put(
            urlString = localHostUrlFactory.createUrl() + path,
        ) {
            contentType(contentType)
            block()
        }

    override suspend fun delete(
        path: String,
        contentType: ContentType,
        block: HttpRequestBuilder.() -> Unit,
    ): HttpResponse =
        httpClient.delete(
            urlString = localHostUrlFactory.createUrl() + path,
        ) {
            contentType(contentType)
            block()
        }
}
