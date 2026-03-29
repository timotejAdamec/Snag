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

package cz.adamec.timotej.snag.authentication.fe.app.impl.internal

import cz.adamec.timotej.snag.authentication.fe.app.api.LoginResult
import cz.adamec.timotej.snag.authentication.fe.app.api.LoginUseCase
import cz.adamec.timotej.snag.authentication.fe.ports.AuthTokenProvider
import cz.adamec.timotej.snag.authentication.fe.ports.AuthenticationApi
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult

internal class LoginUseCaseImpl(
    private val authTokenProvider: AuthTokenProvider,
    private val authenticationApi: AuthenticationApi,
) : LoginUseCase {
    override suspend fun invoke(): LoginResult {
        authTokenProvider.login()
        return when (val result = authenticationApi.getCurrentUser()) {
            is OnlineDataResult.Success -> {
                authTokenProvider.setAuthenticatedUserId(result.data)
                LoginResult.Success
            }
            is OnlineDataResult.Failure -> {
                authTokenProvider.logout()
                LoginResult.Error(message = "Failed to resolve user identity.")
            }
        }
    }
}
