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

import cz.adamec.timotej.snag.authentication.fe.app.api.IsCurrentUserAuthenticatedFlowUseCase
import cz.adamec.timotej.snag.authentication.fe.ports.AuthState
import cz.adamec.timotej.snag.authentication.fe.ports.AuthTokenProvider
import cz.adamec.timotej.snag.core.foundation.common.mapState
import kotlinx.coroutines.flow.StateFlow

internal class IsCurrentUserAuthenticatedFlowUseCaseImpl(
    private val authTokenProvider: AuthTokenProvider,
) : IsCurrentUserAuthenticatedFlowUseCase {
    override fun invoke(): StateFlow<Boolean> =
        authTokenProvider.authState.mapState { state ->
            state is AuthState.Authenticated
        }
}
