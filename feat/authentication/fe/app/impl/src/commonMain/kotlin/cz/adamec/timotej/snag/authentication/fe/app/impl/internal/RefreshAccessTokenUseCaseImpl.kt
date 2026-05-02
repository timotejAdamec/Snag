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

import cz.adamec.timotej.snag.authentication.fe.app.api.RefreshAccessTokenUseCase
import cz.adamec.timotej.snag.authentication.fe.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.authentication.fe.ports.AuthTokenProvider
import cz.adamec.timotej.snag.core.foundation.common.runCatchingCancellable

internal class RefreshAccessTokenUseCaseImpl(
    private val authTokenProvider: AuthTokenProvider,
) : RefreshAccessTokenUseCase {
    override suspend fun invoke(): String? {
        logger.d { "Executing access token refresh." }
        return runCatchingCancellable {
            authTokenProvider.refreshAccessToken()
        }.getOrElse { e ->
            logger.e(throwable = e) { "Access token refresh failed, logging out." }
            authTokenProvider.logout()
            null
        }
    }
}
