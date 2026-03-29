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

import cz.adamec.timotej.snag.authentication.fe.app.api.GetAccessTokenUseCase
import cz.adamec.timotej.snag.authentication.fe.ports.AuthTokenProvider

internal class GetAccessTokenUseCaseImpl(
    private val authTokenProvider: AuthTokenProvider,
) : GetAccessTokenUseCase {
    override suspend fun invoke(): String? = authTokenProvider.getAccessToken()
}
