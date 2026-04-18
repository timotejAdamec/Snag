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
import cz.adamec.timotej.snag.authentication.fe.ports.AuthTokenProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.tokenstore.TokenStore
import org.publicvalue.multiplatform.oidc.types.Jwt

@OptIn(ExperimentalOpenIdConnect::class)
internal class WearAuthTokenProvider(
    private val tokenStore: TokenStore,
    private val authFlow: WearAuthFlow,
) : AuthTokenProvider {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    override val authState: StateFlow<AuthState> = _authState

    override suspend fun restoreSession() {
        val accessToken = tokenStore.getAccessToken()
        if (accessToken.isNullOrBlank()) {
            _authState.value = AuthState.Unauthenticated
            return
        }
        val authProviderId = tokenStore.getIdToken()?.let { extractOidFromIdToken(it) }
        _authState.value =
            if (authProviderId != null) {
                AuthState.Authenticated(authProviderId = authProviderId)
            } else {
                AuthState.Unauthenticated
            }
    }

    override suspend fun login() {
        val result = authFlow.runLoginFlow()
        tokenStore.saveTokens(
            accessToken = result.accessToken,
            refreshToken = result.refreshToken,
            idToken = result.idToken,
        )
        _authState.value = AuthState.Authenticated(authProviderId = result.authProviderId)
    }

    override suspend fun getAccessToken(): String? = tokenStore.getAccessToken()

    override suspend fun logout() {
        tokenStore.saveTokens(accessToken = "", refreshToken = "", idToken = "")
        _authState.value = AuthState.Unauthenticated
    }

    private fun extractOidFromIdToken(idToken: String): String? {
        val jwt = Jwt.parse(idToken)
        return jwt.payload.additionalClaims["oid"] as? String
    }
}
