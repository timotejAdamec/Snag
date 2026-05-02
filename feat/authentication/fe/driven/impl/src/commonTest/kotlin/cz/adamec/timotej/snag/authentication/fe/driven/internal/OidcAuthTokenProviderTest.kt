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

package cz.adamec.timotej.snag.authentication.fe.driven.internal

import cz.adamec.timotej.snag.authentication.fe.ports.AuthState
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.URLBuilder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.OpenIdConnectException
import org.publicvalue.multiplatform.oidc.flows.CodeAuthFlow
import org.publicvalue.multiplatform.oidc.flows.CodeAuthFlowFactory
import org.publicvalue.multiplatform.oidc.flows.EndSessionFlow
import org.publicvalue.multiplatform.oidc.tokenstore.TokenRefreshHandler
import org.publicvalue.multiplatform.oidc.tokenstore.TokenStore
import org.publicvalue.multiplatform.oidc.types.AuthCodeRequest
import org.publicvalue.multiplatform.oidc.types.remote.AccessTokenResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull

@OptIn(ExperimentalOpenIdConnect::class)
class OidcAuthTokenProviderTest {
    @Test
    fun `restoreSession exchanges pending redirect code for tokens and authenticates`() =
        runTest {
            val tokenStore = FakeTokenStore()
            val tokens =
                AccessTokenResponse(
                    access_token = "access-from-redirect",
                    id_token = ID_TOKEN_WITH_OID,
                    refresh_token = "refresh-from-redirect",
                )
            val flow = FakeCodeAuthFlow(canContinue = true, continueResult = Result.success(tokens))
            val provider = newProvider(tokenStore = tokenStore, flow = flow)

            provider.restoreSession()

            assertEquals(expected = "access-from-redirect", actual = tokenStore.savedAccessToken)
            assertEquals(expected = "refresh-from-redirect", actual = tokenStore.savedRefreshToken)
            assertEquals(expected = ID_TOKEN_WITH_OID, actual = tokenStore.savedIdToken)
            val state = provider.authState.value
            assertIs<AuthState.Authenticated>(state)
            assertEquals(expected = "test-auth-provider-id", actual = state.authProviderId)
        }

    @Test
    fun `restoreSession propagates exception from continueLogin without partial token persistence`() =
        runTest {
            val tokenStore = FakeTokenStore()
            val failure = OpenIdConnectException.AuthenticationFailure("token exchange failed")
            val flow =
                FakeCodeAuthFlow(canContinue = true, continueResult = Result.failure(failure))
            val provider = newProvider(tokenStore = tokenStore, flow = flow)

            assertFailsWith<OpenIdConnectException.AuthenticationFailure> {
                provider.restoreSession()
            }
            assertNull(tokenStore.savedAccessToken)
            assertNull(tokenStore.savedRefreshToken)
            assertNull(tokenStore.savedIdToken)
        }

    @Test
    fun `restoreSession authenticates from stored tokens when no pending redirect`() =
        runTest {
            val tokenStore =
                FakeTokenStore(
                    initialAccessToken = "stored-access",
                    initialIdToken = ID_TOKEN_WITH_OID,
                )
            val flow = FakeCodeAuthFlow(canContinue = false)
            val provider = newProvider(tokenStore = tokenStore, flow = flow)

            provider.restoreSession()

            val state = provider.authState.value
            assertIs<AuthState.Authenticated>(state)
            assertEquals(expected = "test-auth-provider-id", actual = state.authProviderId)
        }

    @Test
    fun `restoreSession sets unauthenticated when no pending redirect and no stored tokens`() =
        runTest {
            val tokenStore = FakeTokenStore()
            val flow = FakeCodeAuthFlow(canContinue = false)
            val provider = newProvider(tokenStore = tokenStore, flow = flow)

            provider.restoreSession()

            assertIs<AuthState.Unauthenticated>(provider.authState.value)
        }

    private fun newProvider(
        tokenStore: FakeTokenStore,
        flow: FakeCodeAuthFlow,
    ): OidcAuthTokenProvider =
        OidcAuthTokenProvider(
            tokenStore = tokenStore,
            tokenRefreshHandler = TokenRefreshHandler(tokenStore = tokenStore),
            authFlowFactory = FakeCodeAuthFlowFactory(flow = flow),
            loginExecutor = StandardOidcLoginExecutor(),
            redirectUri = "https://example.test/callback",
        )

    private companion object {
        // Header {"alg":"none","typ":"JWT"} . Payload {"oid":"test-auth-provider-id"} . signature
        const val ID_TOKEN_WITH_OID =
            "eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0." +
                "eyJvaWQiOiJ0ZXN0LWF1dGgtcHJvdmlkZXItaWQifQ." +
                "sig"
    }
}

@OptIn(ExperimentalOpenIdConnect::class)
private class FakeTokenStore(
    initialAccessToken: String? = null,
    initialIdToken: String? = null,
    initialRefreshToken: String? = null,
) : TokenStore() {
    var savedAccessToken: String? = initialAccessToken
        private set
    var savedRefreshToken: String? = initialRefreshToken
        private set
    var savedIdToken: String? = initialIdToken
        private set

    override val accessTokenFlow: MutableStateFlow<String?> = MutableStateFlow(initialAccessToken)
    override val refreshTokenFlow: MutableStateFlow<String?> = MutableStateFlow(initialRefreshToken)
    override val idTokenFlow: MutableStateFlow<String?> = MutableStateFlow(initialIdToken)

    override suspend fun getAccessToken(): String? = savedAccessToken

    override suspend fun getRefreshToken(): String? = savedRefreshToken

    override suspend fun getIdToken(): String? = savedIdToken

    override suspend fun removeAccessToken() {
        savedAccessToken = null
        accessTokenFlow.value = null
    }

    override suspend fun removeRefreshToken() {
        savedRefreshToken = null
        refreshTokenFlow.value = null
    }

    override suspend fun removeIdToken() {
        savedIdToken = null
        idTokenFlow.value = null
    }

    override suspend fun saveTokens(
        accessToken: String,
        refreshToken: String?,
        idToken: String?,
    ) {
        savedAccessToken = accessToken
        savedRefreshToken = refreshToken
        savedIdToken = idToken
        accessTokenFlow.value = accessToken
        refreshTokenFlow.value = refreshToken
        idTokenFlow.value = idToken
    }
}

private class FakeCodeAuthFlow(
    private val canContinue: Boolean,
    private val continueResult: Result<AccessTokenResponse> =
        Result.failure(IllegalStateException("continueLogin not configured")),
) : CodeAuthFlow {
    override suspend fun startLogin(configureAuthUrl: (URLBuilder.() -> Unit)?): AuthCodeRequest = error("not used")

    override suspend fun canContinueLogin(): Boolean = canContinue

    override suspend fun continueLogin(configureTokenExchange: (HttpRequestBuilder.() -> Unit)?): AccessTokenResponse =
        continueResult.getOrThrow()
}

private class FakeCodeAuthFlowFactory(
    private val flow: FakeCodeAuthFlow,
) : CodeAuthFlowFactory {
    override fun createAuthFlow(client: OpenIdConnectClient): CodeAuthFlow = flow

    override fun createEndSessionFlow(client: OpenIdConnectClient): EndSessionFlow = error("not used")
}
