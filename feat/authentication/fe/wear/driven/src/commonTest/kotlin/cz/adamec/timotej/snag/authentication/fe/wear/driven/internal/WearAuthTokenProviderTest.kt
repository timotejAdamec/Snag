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

package cz.adamec.timotej.snag.authentication.fe.wear.driven.internal

import cz.adamec.timotej.snag.authentication.fe.ports.AuthState
import kotlinx.coroutines.test.runTest
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

@OptIn(ExperimentalOpenIdConnect::class, ExperimentalEncodingApi::class)
class WearAuthTokenProviderTest {
    @Test
    fun `restoreSession sets Unauthenticated when no stored access token`() =
        runTest {
            val provider =
                WearAuthTokenProvider(
                    tokenStore = FakeTokenStore(),
                    authFlow = FakeWearAuthFlow(),
                )

            provider.restoreSession()

            assertEquals(AuthState.Unauthenticated, provider.authState.value)
        }

    @Test
    fun `restoreSession sets Authenticated when access and id token with oid present`() =
        runTest {
            val provider =
                WearAuthTokenProvider(
                    tokenStore =
                        FakeTokenStore(
                            accessToken = "stored-access",
                            idToken = buildIdToken(oid = "user-42"),
                        ),
                    authFlow = FakeWearAuthFlow(),
                )

            provider.restoreSession()

            assertEquals(
                AuthState.Authenticated(authProviderId = "user-42"),
                provider.authState.value,
            )
        }

    @Test
    fun `restoreSession sets Unauthenticated when id token missing oid claim`() =
        runTest {
            val provider =
                WearAuthTokenProvider(
                    tokenStore =
                        FakeTokenStore(
                            accessToken = "stored-access",
                            idToken = buildIdToken(oid = null),
                        ),
                    authFlow = FakeWearAuthFlow(),
                )

            provider.restoreSession()

            assertEquals(AuthState.Unauthenticated, provider.authState.value)
        }

    @Test
    fun `login persists tokens and flips state to Authenticated`() =
        runTest {
            val tokenStore = FakeTokenStore()
            val authFlow =
                FakeWearAuthFlow(
                    nextResult =
                        WearLoginResult(
                            accessToken = "a",
                            refreshToken = "r",
                            idToken = "i",
                            authProviderId = "user-99",
                        ),
                )
            val provider = WearAuthTokenProvider(tokenStore = tokenStore, authFlow = authFlow)

            provider.login()

            assertEquals("a", tokenStore.getAccessToken())
            assertEquals("r", tokenStore.getRefreshToken())
            assertEquals("i", tokenStore.getIdToken())
            assertEquals(
                AuthState.Authenticated(authProviderId = "user-99"),
                provider.authState.value,
            )
        }

    @Test
    fun `login propagates exception and leaves token store unchanged`() =
        runTest {
            val tokenStore = FakeTokenStore()
            val authFlow = FakeWearAuthFlow(nextError = IllegalStateException("boom"))
            val provider = WearAuthTokenProvider(tokenStore = tokenStore, authFlow = authFlow)

            assertFailsWith<IllegalStateException> { provider.login() }
            assertNull(tokenStore.getAccessToken())
        }

    @Test
    fun `logout clears stored tokens and flips state to Unauthenticated`() =
        runTest {
            val tokenStore =
                FakeTokenStore(
                    accessToken = "a",
                    refreshToken = "r",
                    idToken = "i",
                )
            val provider = WearAuthTokenProvider(tokenStore = tokenStore, authFlow = FakeWearAuthFlow())

            provider.logout()

            assertEquals("", tokenStore.getAccessToken())
            assertEquals(AuthState.Unauthenticated, provider.authState.value)
        }

    @Test
    fun `getAccessToken delegates to token store`() =
        runTest {
            val provider =
                WearAuthTokenProvider(
                    tokenStore = FakeTokenStore(accessToken = "delegated"),
                    authFlow = FakeWearAuthFlow(),
                )

            assertEquals("delegated", provider.getAccessToken())
        }

    private fun buildIdToken(oid: String?): String {
        val header = """{"alg":"none","typ":"JWT"}"""
        val payload = if (oid == null) "{}" else """{"oid":"$oid"}"""
        return "${b64Url(header)}.${b64Url(payload)}.sig"
    }

    private fun b64Url(value: String): String = Base64.UrlSafe.encode(value.encodeToByteArray()).trimEnd('=')
}
