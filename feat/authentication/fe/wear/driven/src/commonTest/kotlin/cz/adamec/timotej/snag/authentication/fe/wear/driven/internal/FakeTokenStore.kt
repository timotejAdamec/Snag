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

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.tokenstore.TokenStore

@OptIn(ExperimentalOpenIdConnect::class)
internal class FakeTokenStore(
    accessToken: String? = null,
    refreshToken: String? = null,
    idToken: String? = null,
) : TokenStore() {
    private val _accessTokenFlow = MutableStateFlow(accessToken)
    private val _refreshTokenFlow = MutableStateFlow(refreshToken)
    private val _idTokenFlow = MutableStateFlow(idToken)

    override val accessTokenFlow: StateFlow<String?> = _accessTokenFlow
    override val refreshTokenFlow: StateFlow<String?> = _refreshTokenFlow
    override val idTokenFlow: StateFlow<String?> = _idTokenFlow

    override suspend fun getAccessToken(): String? = _accessTokenFlow.value

    override suspend fun getRefreshToken(): String? = _refreshTokenFlow.value

    override suspend fun getIdToken(): String? = _idTokenFlow.value

    override suspend fun removeAccessToken() {
        _accessTokenFlow.value = null
    }

    override suspend fun removeRefreshToken() {
        _refreshTokenFlow.value = null
    }

    override suspend fun removeIdToken() {
        _idTokenFlow.value = null
    }

    override suspend fun saveTokens(
        accessToken: String,
        refreshToken: String?,
        idToken: String?,
    ) {
        _accessTokenFlow.value = accessToken
        _refreshTokenFlow.value = refreshToken
        _idTokenFlow.value = idToken
    }
}
