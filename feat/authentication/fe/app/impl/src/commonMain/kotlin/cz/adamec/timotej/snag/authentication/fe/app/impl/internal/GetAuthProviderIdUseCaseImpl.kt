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

import cz.adamec.timotej.snag.authentication.fe.app.api.GetAuthProviderIdUseCase
import cz.adamec.timotej.snag.authentication.fe.ports.AuthState
import cz.adamec.timotej.snag.authentication.fe.ports.AuthTokenProvider
import cz.adamec.timotej.snag.core.foundation.common.mapState
import kotlinx.coroutines.flow.Flow

internal class GetAuthProviderIdUseCaseImpl(
    authTokenProvider: AuthTokenProvider,
) : GetAuthProviderIdUseCase {
    private val authProviderIdFlow =
        authTokenProvider.authState.mapState { state ->
            when (state) {
                is AuthState.Authenticated -> state.authProviderId
                is AuthState.Unauthenticated -> null
            }
        }

    override fun invoke(): Flow<String?> = authProviderIdFlow
}
