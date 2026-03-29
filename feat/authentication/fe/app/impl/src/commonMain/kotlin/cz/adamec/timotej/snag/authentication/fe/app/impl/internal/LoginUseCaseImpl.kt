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

internal class LoginUseCaseImpl(
    private val authTokenProvider: AuthTokenProvider,
) : LoginUseCase {
    @Suppress("TooGenericExceptionCaught")
    override suspend fun invoke(): LoginResult =
        try {
            authTokenProvider.login()
            LoginResult.Success
        } catch (e: Exception) {
            LoginResult.Error(message = e.message ?: "Login failed.")
        }
}
