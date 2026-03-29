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

import cz.adamec.timotej.snag.authentication.fe.app.api.GetAuthenticatedUserIdUseCase
import cz.adamec.timotej.snag.authentication.fe.ports.AuthState
import cz.adamec.timotej.snag.authentication.fe.ports.AuthTokenProvider
import cz.adamec.timotej.snag.core.foundation.common.mapState
import kotlinx.coroutines.flow.StateFlow
import kotlin.uuid.Uuid

internal class GetAuthenticatedUserIdUseCaseImpl(
    authTokenProvider: AuthTokenProvider,
) : GetAuthenticatedUserIdUseCase {
    override val currentUserId: StateFlow<Uuid?> =
        authTokenProvider.authState.mapState { state ->
            when (state) {
                is AuthState.Authenticated -> state.userId
                is AuthState.Unauthenticated -> null
            }
        }

    override fun requireCurrentUserId(): Uuid = currentUserId.value ?: error("User must be authenticated first.")
}
