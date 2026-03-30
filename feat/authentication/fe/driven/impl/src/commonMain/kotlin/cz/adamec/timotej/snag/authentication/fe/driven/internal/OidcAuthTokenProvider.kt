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

import cz.adamec.timotej.snag.authentication.fe.driven.internal.LH.logger
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
    redirectUri: String,
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
            this.redirectUri = redirectUri
        }

    override suspend fun restoreSession() {
        logger.d { "Restoring session." }
        val accessToken = tokenStore.getAccessToken()
        if (accessToken.isNullOrBlank()) {
            logger.d { "No stored access token found, setting state to Unauthenticated." }
            _authState.value = AuthState.Unauthenticated
            return
        }
        val idToken = tokenStore.getIdToken()
        val authProviderId = idToken?.let { extractOidFromIdToken(it) }
        _authState.value =
            if (authProviderId != null) {
                logger.d { "Restored session for authProviderId=$authProviderId." }
                AuthState.Authenticated(authProviderId = authProviderId)
            } else {
                logger.d { "No OID claim in ID token, setting state to Unauthenticated." }
                AuthState.Unauthenticated
            }
    }

    override suspend fun login() {
        logger.d { "Starting OIDC login flow." }
        val flow = authFlowFactory.createAuthFlow(client)
        val tokens = flow.getAccessToken()
        logger.d { "Received tokens from OIDC provider, saving to token store." }
        tokenStore.saveTokens(
            accessToken = tokens.access_token,
            refreshToken = tokens.refresh_token,
            idToken = tokens.id_token,
        )
        val authProviderId =
            tokens.id_token?.let { extractOidFromIdToken(it) }
                ?: error("ID token missing or does not contain oid claim after successful login.")
        logger.d { "Login successful for authProviderId=$authProviderId." }
        _authState.value = AuthState.Authenticated(authProviderId = authProviderId)
    }

    override suspend fun getAccessToken(): String? {
        logger.d { "Getting access token from token store." }
        return tokenStore.getAccessToken().also {
            logger.d { "Got access token: present=${it != null}." }
        }
    }

    override suspend fun logout() {
        logger.d { "Logging out, clearing token store." }
        tokenStore.saveTokens(accessToken = "", refreshToken = "", idToken = "")
        _authState.value = AuthState.Unauthenticated
        logger.d { "Logged out, state set to Unauthenticated." }
    }

    private fun extractOidFromIdToken(idToken: String): String? {
        val jwt = Jwt.parse(idToken)
        return jwt.payload.additionalClaims["oid"] as? String
    }
}
