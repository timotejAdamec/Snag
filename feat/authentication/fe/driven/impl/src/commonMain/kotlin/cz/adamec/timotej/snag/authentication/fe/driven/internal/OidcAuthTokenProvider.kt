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
import cz.adamec.timotej.snag.authentication.fe.ports.AuthTokenProvider
import cz.adamec.timotej.snag.configuration.common.CommonConfiguration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.flows.CodeAuthFlowFactory
import org.publicvalue.multiplatform.oidc.tokenstore.TokenStore
import org.publicvalue.multiplatform.oidc.types.CodeChallengeMethod
import org.publicvalue.multiplatform.oidc.types.Jwt

internal class OidcAuthTokenProvider(
    private val tokenStore: TokenStore,
    private val authFlowFactory: CodeAuthFlowFactory,
) : AuthTokenProvider {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    override val authState: StateFlow<AuthState> = _authState

    private val client =
        OpenIdConnectClient(
            discoveryUri = "https://login.microsoftonline.com/${CommonConfiguration.entraIdTenantId}/v2.0/.well-known/openid-configuration",
        ) {
            clientId = CommonConfiguration.entraIdClientId
            codeChallengeMethod = CodeChallengeMethod.S256
            scope = "openid profile email offline_access"
            redirectUri = CommonConfiguration.entraIdRedirectUri
        }

    override suspend fun restoreSession() {
        val accessToken = tokenStore.getAccessToken()
        if (accessToken.isNullOrBlank()) {
            _authState.value = AuthState.Unauthenticated
            return
        }
        val idToken = tokenStore.getIdToken()
        val authProviderId = idToken?.let { extractOidFromIdToken(it) }
        _authState.value =
            if (authProviderId != null) {
                AuthState.Authenticated(authProviderId = authProviderId)
            } else {
                AuthState.Unauthenticated
            }
    }

    override suspend fun login() {
        val flow = authFlowFactory.createAuthFlow(client)
        val tokens = flow.getAccessToken()
        tokenStore.saveTokens(
            accessToken = tokens.access_token,
            refreshToken = tokens.refresh_token,
            idToken = tokens.id_token,
        )
        val authProviderId = tokens.id_token?.let { extractOidFromIdToken(it) }
        if (authProviderId != null) {
            _authState.value = AuthState.Authenticated(authProviderId = authProviderId)
        }
    }

    override suspend fun getAccessToken(): String? = tokenStore.getAccessToken()

    override suspend fun logout() {
        tokenStore.saveTokens(accessToken = "", refreshToken = "", idToken = "")
        _authState.value = AuthState.Unauthenticated
    }

    private fun extractOidFromIdToken(idToken: String): String? =
        try {
            val jwt = Jwt.parse(idToken)
            jwt.payload.additionalClaims["oid"] as? String
        } catch (_: Exception) {
            null
        }
}
