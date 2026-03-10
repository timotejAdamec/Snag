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

package cz.adamec.timotej.snag.network.fe.app.impl.internal

import cz.adamec.timotej.snag.network.fe.SnagNetworkHttpClient
import cz.adamec.timotej.snag.network.fe.ports.ServerUrlFactory
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType

internal class SnagNetworkHttpClientImpl(
    private val httpClient: HttpClient,
    private val serverUrlFactory: ServerUrlFactory,
) : SnagNetworkHttpClient {
    override suspend fun get(
        path: String,
        block: HttpRequestBuilder.() -> Unit,
    ): HttpResponse =
        httpClient.get(
            urlString = serverUrlFactory.createUrl() + path,
        ) {
            block()
        }

    override suspend fun post(
        path: String,
        contentType: ContentType,
        block: HttpRequestBuilder.() -> Unit,
    ): HttpResponse =
        httpClient.post(
            urlString = serverUrlFactory.createUrl() + path,
        ) {
            contentType(contentType)
            block()
        }

    override suspend fun put(
        path: String,
        contentType: ContentType,
        block: HttpRequestBuilder.() -> Unit,
    ): HttpResponse =
        httpClient.put(
            urlString = serverUrlFactory.createUrl() + path,
        ) {
            contentType(contentType)
            block()
        }

    override suspend fun patch(
        path: String,
        contentType: ContentType,
        block: HttpRequestBuilder.() -> Unit,
    ): HttpResponse =
        httpClient.patch(
            urlString = serverUrlFactory.createUrl() + path,
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
            urlString = serverUrlFactory.createUrl() + path,
        ) {
            contentType(contentType)
            block()
        }
}
