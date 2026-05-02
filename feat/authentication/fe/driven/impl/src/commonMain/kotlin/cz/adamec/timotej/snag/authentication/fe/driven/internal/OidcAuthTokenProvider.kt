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
import cz.adamec.timotej.snag.configuration.common.RunConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.flows.CodeAuthFlowFactory
import org.publicvalue.multiplatform.oidc.tokenstore.TokenStore
import org.publicvalue.multiplatform.oidc.types.CodeChallengeMethod
import org.publicvalue.multiplatform.oidc.types.Jwt
import org.publicvalue.multiplatform.oidc.types.remote.AccessTokenResponse

internal class OidcAuthTokenProvider(
    private val tokenStore: TokenStore,
    authFlowFactory: CodeAuthFlowFactory,
    private val loginExecutor: OidcLoginExecutor,
    redirectUri: String,
) : AuthTokenProvider {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    override val authState: StateFlow<AuthState> = _authState

    private val client =
        OpenIdConnectClient {
            endpoints {
                authorizationEndpoint = "https://login.microsoftonline.com/${RunConfig.entraIdTenantId}/oauth2/v2.0/authorize"
                tokenEndpoint = "https://login.microsoftonline.com/${RunConfig.entraIdTenantId}/oauth2/v2.0/token"
                endSessionEndpoint = "https://login.microsoftonline.com/${RunConfig.entraIdTenantId}/oauth2/v2.0/logout"
            }
            clientId = RunConfig.entraIdClientId
            codeChallengeMethod = CodeChallengeMethod.S256
            scope = "openid profile email offline_access api://${RunConfig.entraIdClientId}/access_as_user"
            this.redirectUri = redirectUri
        }

    private val flow = authFlowFactory.createAuthFlow(client)

    override suspend fun restoreSession() {
        logger.d { "Restoring session." }
        if (flow.canContinueLogin()) {
            logger.d { "Pending OIDC redirect detected, exchanging code for tokens." }
            val tokens = flow.continueLogin()
            saveTokens(tokens)
        }
        applyAuthStateFromStoredTokens()
    }

    override suspend fun login() {
        logger.d { "Starting OIDC login flow." }
        val tokens = loginExecutor.execute(flow)
        if (tokens == null) {
            logger.d { "Login execution handed off to platform (e.g. web redirect); state will be set after redirect." }
            return
        }
        saveTokens(tokens)
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

    private suspend fun saveTokens(tokens: AccessTokenResponse) {
        logger.d { "Saving tokens to token store." }
        tokenStore.saveTokens(
            accessToken = tokens.access_token,
            refreshToken = tokens.refresh_token,
            idToken = tokens.id_token,
        )
    }

    private suspend fun applyAuthStateFromStoredTokens() {
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

    private fun extractOidFromIdToken(idToken: String): String? {
        val jwt = Jwt.parse(idToken)
        return jwt.payload.additionalClaims["oid"] as? String
    }
}
