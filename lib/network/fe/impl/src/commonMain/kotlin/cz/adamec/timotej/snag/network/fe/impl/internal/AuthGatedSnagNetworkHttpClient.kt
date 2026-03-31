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

package cz.adamec.timotej.snag.network.fe.impl.internal

import cz.adamec.timotej.snag.network.fe.SnagNetworkHttpClient
import cz.adamec.timotej.snag.network.fe.ports.AuthStateProvider
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import kotlinx.coroutines.flow.first

internal class AuthGatedSnagNetworkHttpClient(
    private val delegate: SnagNetworkHttpClient,
    private val authStateProvider: AuthStateProvider,
) : SnagNetworkHttpClient {
    override suspend fun get(
        path: String,
        block: HttpRequestBuilder.() -> Unit,
    ): HttpResponse {
        awaitAuthenticated()
        return delegate.get(path = path, block = block)
    }

    override suspend fun post(
        path: String,
        contentType: ContentType,
        block: HttpRequestBuilder.() -> Unit,
    ): HttpResponse {
        awaitAuthenticated()
        return delegate.post(path = path, contentType = contentType, block = block)
    }

    override suspend fun put(
        path: String,
        contentType: ContentType,
        block: HttpRequestBuilder.() -> Unit,
    ): HttpResponse {
        awaitAuthenticated()
        return delegate.put(path = path, contentType = contentType, block = block)
    }

    override suspend fun patch(
        path: String,
        contentType: ContentType,
        block: HttpRequestBuilder.() -> Unit,
    ): HttpResponse {
        awaitAuthenticated()
        return delegate.patch(path = path, contentType = contentType, block = block)
    }

    override suspend fun delete(
        path: String,
        contentType: ContentType,
        block: HttpRequestBuilder.() -> Unit,
    ): HttpResponse {
        awaitAuthenticated()
        return delegate.delete(path = path, contentType = contentType, block = block)
    }

    private suspend fun awaitAuthenticated() {
        authStateProvider.isReady.first { it }
    }
}
