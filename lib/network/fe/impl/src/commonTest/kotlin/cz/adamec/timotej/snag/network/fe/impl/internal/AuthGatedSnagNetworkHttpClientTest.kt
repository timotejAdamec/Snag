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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AuthGatedSnagNetworkHttpClientTest {
    private val authState = MutableStateFlow(false)
    private val authStateProvider =
        object : AuthStateProvider {
            override val isReady: StateFlow<Boolean> = authState
        }

    private val delegateCalls = mutableListOf<String>()
    private val delegate =
        object : SnagNetworkHttpClient {
            override suspend fun get(
                path: String,
                block: HttpRequestBuilder.() -> Unit,
            ): HttpResponse {
                delegateCalls.add("GET $path")
                error("stub")
            }

            override suspend fun post(
                path: String,
                contentType: ContentType,
                block: HttpRequestBuilder.() -> Unit,
            ): HttpResponse {
                delegateCalls.add("POST $path")
                error("stub")
            }

            override suspend fun put(
                path: String,
                contentType: ContentType,
                block: HttpRequestBuilder.() -> Unit,
            ): HttpResponse {
                delegateCalls.add("PUT $path")
                error("stub")
            }

            override suspend fun patch(
                path: String,
                contentType: ContentType,
                block: HttpRequestBuilder.() -> Unit,
            ): HttpResponse {
                delegateCalls.add("PATCH $path")
                error("stub")
            }

            override suspend fun delete(
                path: String,
                contentType: ContentType,
                block: HttpRequestBuilder.() -> Unit,
            ): HttpResponse {
                delegateCalls.add("DELETE $path")
                error("stub")
            }
        }

    private fun createGatedClient() =
        AuthGatedSnagNetworkHttpClient(
            delegate = delegate,
            authStateProvider = authStateProvider,
        )

    @Test
    fun `get request suspends when auth is not ready and proceeds when ready`() =
        runTest {
            val client = createGatedClient()
            var completed = false
            val job =
                launch {
                    try {
                        client.get("/test")
                    } catch (_: Exception) {
                    }
                    completed = true
                }
            advanceUntilIdle()
            assertFalse(completed)
            assertTrue(delegateCalls.isEmpty())

            authState.value = true
            advanceUntilIdle()
            assertTrue(completed)
            assertEquals(expected = listOf("GET /test"), actual = delegateCalls)
            job.cancel()
        }

    @Test
    fun `post request suspends when auth is not ready and proceeds when ready`() =
        runTest {
            val client = createGatedClient()
            var completed = false
            val job =
                launch {
                    try {
                        client.post("/test")
                    } catch (_: Exception) {
                    }
                    completed = true
                }
            advanceUntilIdle()
            assertFalse(completed)

            authState.value = true
            advanceUntilIdle()
            assertTrue(completed)
            assertEquals(expected = listOf("POST /test"), actual = delegateCalls)
            job.cancel()
        }

    @Test
    fun `request proceeds immediately when auth is already ready`() =
        runTest {
            authState.value = true
            val client = createGatedClient()
            var completed = false
            launch {
                try {
                    client.get("/test")
                } catch (_: Exception) {
                }
                completed = true
            }
            advanceUntilIdle()
            assertTrue(completed)
            assertEquals(expected = listOf("GET /test"), actual = delegateCalls)
        }
}
